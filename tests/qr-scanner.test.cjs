// Lifecycle tests with a mocked html5-qrcode API; not a physical camera test.
const fs = require('fs'), vm = require('vm'), assert = require('assert/strict');
const source = fs.readFileSync(require('path').join(__dirname, '../src/main/webapp/assets/js/qr-scanner.js'), 'utf8');
function harness(options = {}) {
 const state = { starts: [], stops: 0, submits: 0, scans: 0, events: {} };
 const element = () => ({ handlers: {}, value: '', textContent: '', disabled: false, files: [], options: [], addEventListener(k,f){this.handlers[k]=f;},setAttribute(){},replaceChildren(){this.options=[];},appendChild(o){this.options.push(o);} });
 const ids = Object.fromEntries(['lookup','cameraButton','cameraSelect','qrImage','cameraStatus','ticketInput'].map(k=>[k,element()]));
 ids.lookup.requestSubmit = () => { state.submits++; ids.lookup.handlers.submit(); };
 class Engine {
  constructor(id,config){state.config=config;}
  async start(device,config,success){state.starts.push(device);state.success=success;if(options.pending)await options.pending;if(options.error)throw options.error;}
  async stop(){state.stops++;} clear(){} getRunningTrackSettings(){return {deviceId:'rear'};}
  async scanFile(){state.scans++;if(options.fileError)throw Error('decode');return options.code||'TK123';}
 }
 const document = {hidden:false,getElementById:k=>ids[k],createElement:element,querySelectorAll:()=>[],addEventListener(k,f){state.events[k]=f;}};
 const window = {isSecureContext:options.secure!==false,Html5Qrcode:options.missing?null:Engine,Html5QrcodeSupportedFormats:{QR_CODE:0},addEventListener(k,f){state.events[k]=f;}};
 vm.runInNewContext(source,{window,document,navigator:{mediaDevices:{enumerateDevices:async()=>[{kind:'videoinput',deviceId:'rear',label:'Rear'}]}}});
 return {ids,state,document,async flush(){for(let i=0;i<20;i++)await new Promise(r=>setImmediate(r));}};
}
(async()=>{
 let h=harness();h.ids.cameraButton.handlers.click();await h.flush();assert.equal(h.state.starts.length,1);assert.equal(h.state.config.experimentalFeatures.useBarCodeDetectorIfSupported,false);h.state.success(' TK123 ');h.state.success('TK123');await h.flush();assert.equal(h.state.submits,1);assert.equal(h.ids.ticketInput.value,'TK123');assert.equal(h.state.stops,1);
 h=harness();h.ids.cameraButton.handlers.click();await h.flush();h.state.success('https://example.org/');await h.flush();assert.equal(h.state.submits,0);assert.match(h.ids.cameraStatus.textContent,/không phải/);
 h=harness({secure:false});h.ids.cameraButton.handlers.click();await h.flush();assert.equal(h.state.starts.length,0);assert.match(h.ids.cameraStatus.textContent,/HTTPS/);
 h=harness({error:{name:'NotAllowedError'}});h.ids.cameraButton.handlers.click();await h.flush();assert.match(h.ids.cameraStatus.textContent,/cho phép/);
 h=harness();h.ids.cameraButton.handlers.click();await h.flush();h.ids.cameraSelect.value='front';h.ids.cameraSelect.handlers.change();await h.flush();assert.equal(h.state.stops,1);assert.equal(h.state.starts[1],'front');
 let resolve;h=harness({pending:new Promise(r=>resolve=r)});h.ids.cameraButton.handlers.click();await h.flush();h.ids.cameraButton.handlers.click();resolve();await h.flush();assert.equal(h.state.stops,1);assert.equal(h.state.submits,0);
 h=harness();h.ids.cameraButton.handlers.click();await h.flush();h.document.hidden=true;h.state.events.visibilitychange();await h.flush();h.state.success('TK123');await h.flush();assert.equal(h.state.stops,1);assert.equal(h.state.submits,0);
 h=harness({secure:false});h.ids.qrImage.files=[{type:'image/png',size:100}];h.ids.qrImage.handlers.change();await h.flush();assert.equal(h.state.scans,1);assert.equal(h.state.submits,1);
 h=harness({fileError:true});h.ids.qrImage.files=[{type:'image/png',size:100}];h.ids.qrImage.handlers.change();await h.flush();assert.equal(h.state.submits,0);assert.match(h.ids.cameraStatus.textContent,/Chưa đọc/);
 h=harness({missing:true});h.ids.cameraButton.handlers.click();await h.flush();assert.match(h.ids.cameraStatus.textContent,/Không tải được/);
 console.log('PASS 10 html5-qrcode lifecycle scenarios (mock API, no real camera).');
})().catch(e=>{console.error(e);process.exit(1);});
