/* html5-qrcode 2.3.8: WebRTC capture and decoding stay on this device. */
(() => {
    'use strict';
    const form = document.getElementById('lookup');
    if (!form) return;
    const button = document.getElementById('cameraButton');
    const select = document.getElementById('cameraSelect');
    const fileInput = document.getElementById('qrImage');
    const status = document.getElementById('cameraStatus');
    const input = document.getElementById('ticketInput');
    let scanner, active = false, starting = false, submitted = false, generation = 0;
    let queue = Promise.resolve();
    const message = text => { status.textContent = text; };
    const enqueue = task => { queue = queue.then(task).catch(() => message('Không thể xử lý QR. Vui lòng thử lại hoặc nhập mã vé.')); return queue; };
    function controls() {
        button.textContent = active || starting ? 'Dừng camera' : 'Mở camera quét QR';
        button.setAttribute('aria-pressed', String(active || starting));
        select.disabled = !active;
    }
    function reader() {
        if (!window.Html5Qrcode) throw new Error('LIBRARY_MISSING');
        if (!scanner) scanner = new window.Html5Qrcode('qrReader', {
            formatsToSupport: [window.Html5QrcodeSupportedFormats.QR_CODE],
            experimentalFeatures: { useBarCodeDetectorIfSupported: false },
            verbose: false
        });
        return scanner;
    }
    async function stop() {
        if (scanner && active) {
            try { await scanner.stop(); }
            catch (_) {
                // Release tracks even if a browser closes its stream unexpectedly.
                document.querySelectorAll('#qrReader video').forEach(video => {
                    if (video.srcObject) video.srcObject.getTracks().forEach(track => track.stop());
                });
                scanner = null;
            }
        }
        active = false; starting = false;
        if (scanner) { try { scanner.clear(); } catch (_) {} }
        controls();
    }
    function accept(text, run) {
        if (run !== generation || submitted) return;
        const code = String(text).trim();
        if (!/^[A-Za-z0-9_-]{1,40}$/.test(code)) {
            message('Đã đọc được QR nhưng không phải mã vé F-Cinema. Hãy quét QR trong Vé của tôi.');
            return;
        }
        submitted = true; generation++;
        input.value = code;
        message('Đã đọc mã ' + code + '. Đang kiểm tra vé…');
        enqueue(async () => { await stop(); form.requestSubmit(); });
    }
    function cameraError(error) {
        const value = String(error && (error.name + ' ' + error.message) || error);
        if (/LIBRARY_MISSING/.test(value)) return 'Không tải được html5-qrcode. Hãy triển khai đủ thư mục assets/vendor rồi tải lại trang.';
        if (/NotAllowed|Permission|denied/i.test(value)) return 'Bạn cần cho phép Camera trong trình duyệt và cài đặt hệ điều hành.';
        if (/NotFound|DevicesNotFound/i.test(value)) return 'Không tìm thấy camera. Bạn có thể chọn ảnh QR hoặc nhập mã vé.';
        if (/NotReadable|TrackStart/i.test(value)) return 'Camera đang bận hoặc không mở được. Hãy đóng ứng dụng đang dùng camera.';
        return 'Không mở được camera. Kiểm tra quyền Camera, dùng HTTPS hoặc localhost và thử lại.';
    }
    async function start(run, device) {
        await stop();
        if (run !== generation || submitted) return;
        if (!window.isSecureContext || !navigator.mediaDevices) {
            message('Camera cần HTTPS hoặc localhost. Trên điện thoại truy cập IP mạng LAN, hãy dùng HTTPS; bạn vẫn có thể chọn ảnh QR.');
            return;
        }
        starting = true; controls(); message('Đang xin quyền mở camera…');
        try {
            const engine = reader();
            const onScan = text => accept(text, run);
            try {
                // Scan the full frame to avoid cropping large or nearby ticket codes.
                await engine.start(device || { facingMode: 'environment' }, { fps: 12 }, onScan, () => {});
            } catch (error) {
                if (device || !/NotFound|Overconstrained/.test(String(error && (error.name + ' ' + error.message) || error)) || run !== generation) throw error;
                await engine.start({ facingMode: 'user' }, { fps: 12 }, onScan, () => {});
            }
            active = true; starting = false;
            if (run !== generation) { await stop(); return; }
            controls(); message('Camera đang quét. Giữ QR rõ nét, tránh chói sáng; thử đưa mã ra xa nếu bị mờ.');
            try {
                const devices = await navigator.mediaDevices.enumerateDevices();
                if (run !== generation) return;
                const current = engine.getRunningTrackSettings().deviceId || device || '';
                select.replaceChildren();
                for (const camera of devices.filter(d => d.kind === 'videoinput')) {
                    const option = document.createElement('option');
                    option.value = camera.deviceId; option.textContent = camera.label || 'Camera ' + (select.options.length + 1);
                    select.appendChild(option);
                }
                select.value = current;
            } catch (_) { /* Scanning can continue without a device list. */ }
        } catch (error) {
            await stop();
            if (run === generation) message(cameraError(error));
        }
    }
    button.addEventListener('click', () => {
        const run = ++generation;
        if (active || starting) enqueue(async () => { await stop(); message('Đã dừng camera.'); });
        else { starting = true; controls(); enqueue(() => start(run, select.value)); }
    });
    select.addEventListener('change', () => {
        const run = ++generation, device = select.value;
        enqueue(() => start(run, device));
    });
    fileInput.addEventListener('change', () => {
        const file = fileInput.files[0], run = ++generation;
        enqueue(async () => {
            await stop();
            if (!file || run !== generation || submitted) return;
            if (file.size > 10 * 1024 * 1024 || !/^image\/(jpeg|png|webp)$/.test(file.type)) {
                message('Chọn ảnh JPG, PNG hoặc WebP không quá 10 MB.'); return;
            }
            message('Đang đọc QR từ ảnh trên thiết bị…');
            try { accept(await reader().scanFile(file, true), run); }
            catch (error) { if (run === generation) message(String(error).includes('LIBRARY_MISSING') ? cameraError(error) : 'Chưa đọc được QR trong ảnh. Chọn ảnh rõ nét chứa toàn bộ mã và viền trắng.'); }
            finally { fileInput.value = ''; }
        });
    });
    const leave = () => { generation++; enqueue(stop); };
    form.addEventListener('submit', leave);
    window.addEventListener('pagehide', leave);
    document.addEventListener('visibilitychange', () => { if (document.hidden) leave(); });
    controls();
})();
