<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<style>

    .table-pagination {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;

        margin-top: 16px;
        padding: 12px 14px;

        background: #18181d;
        border: 1px solid #303039;
        border-radius: 8px;

        color: #aaa;
        font-size: 13px;
    }

    .pagination-left {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .pagination-size {
        padding: 7px 9px;

        background: #24242a;
        color: white;

        border: 1px solid #44444d;
        border-radius: 5px;

        outline: none;
        cursor: pointer;

        font-family: inherit;
        font-size: 13px;
    }

    .pagination-controls {
        display: flex;
        align-items: center;
        gap: 5px;
        flex-wrap: wrap;
    }

    .pagination-button {
        min-width: 34px;
        height: 34px;

        padding: 0 9px;

        background: #25252b;
        color: #bbb;

        border: 1px solid #3f3f48;
        border-radius: 5px;

        cursor: pointer;

        font-family: inherit;
        font-size: 12px;
    }

    .pagination-button:hover:not(:disabled) {
        border-color: #ff1744;
        color: white;
    }

    .pagination-button.active {
        background: #ff1744;
        border-color: #ff1744;
        color: white;

        font-weight: bold;
    }

    .pagination-button:disabled {
        opacity: 0.35;
        cursor: default;
    }

    .pagination-dots {
        padding: 0 4px;
        color: #666;
    }

    @media (max-width: 650px) {

        .table-pagination {
            flex-direction: column;
            align-items: flex-start;
        }

    }

</style>


<script>

    (function () {

        var tables =
                document.querySelectorAll(
                        'table[data-paginate="true"], .movies-grid[data-paginate="true"]'
                        );


        tables.forEach(
                function (table) {

                    var dataRows;

                    if (table.tagName === "TABLE") {

                        var tbody = table.tBodies[0];

                        if (!tbody) {
                            return;
                        }

                        var rows = Array.from(tbody.rows);

                        dataRows = rows.filter(
                                function (row) {
                                    return !row.querySelector("td[colspan]");
                                }
                        );

                    } else {

                        dataRows = Array.from(
                                table.querySelectorAll(".movies-card")
                                );
                    }


                    if (dataRows.length === 0) {
                        return;
                    }


                    var pageSize =
                            parseInt(
                                    table.dataset.pageSize || "10",
                                    10
                                    );


                    var currentPage =
                            1;


                    var wrapper =
                            table.closest(
                                    ".movie-card, "
                                    + ".table-card, "
                                    + ".showtime-table-wrap"
                                    );


                    if (!wrapper) {
                        wrapper = table;
                    }


                    var pagination =
                            document.createElement(
                                    "div"
                                    );


                    pagination.className =
                            "table-pagination";


                    var left =
                            document.createElement(
                                    "div"
                                    );


                    left.className =
                            "pagination-left";


                    var sizeLabel =
                            document.createElement(
                                    "span"
                                    );


                    /*
                     * Hiển thị
                     * Dùng Unicode escape để tránh lỗi encoding.
                     */
                    sizeLabel.textContent =
                            "Hi\u1EC3n th\u1ECB";


                    var sizeSelect =
                            document.createElement(
                                    "select"
                                    );


                    sizeSelect.className =
                            "pagination-size";


                    [10, 20, 50].forEach(
                            function (size) {

                                var option =
                                        document.createElement(
                                                "option"
                                                );


                                option.value =
                                        size;


                                /*
                                 * dòng
                                 */
                                option.textContent =
                                        size
                                        + " d\u00F2ng";


                                if (
                                        size
                                        === pageSize
                                        ) {

                                    option.selected =
                                            true;
                                }


                                sizeSelect.appendChild(
                                        option
                                        );
                            }
                    );


                    var info =
                            document.createElement(
                                    "span"
                                    );


                    left.appendChild(
                            sizeLabel
                            );


                    left.appendChild(
                            sizeSelect
                            );


                    left.appendChild(
                            info
                            );


                    var controls =
                            document.createElement(
                                    "div"
                                    );


                    controls.className =
                            "pagination-controls";


                    pagination.appendChild(
                            left
                            );


                    pagination.appendChild(
                            controls
                            );


                    wrapper.insertAdjacentElement(
                            "afterend",
                            pagination
                            );


                    function totalPages() {

                        return Math.max(
                                1,
                                Math.ceil(
                                        dataRows.length
                                        / pageSize
                                        )
                                );
                    }


                    function createButton(
                            buttonText,
                            page,
                            active,
                            disabled) {

                        var button =
                                document.createElement(
                                        "button"
                                        );


                        button.type =
                                "button";


                        button.className =
                                "pagination-button";


                        button.textContent =
                                buttonText;


                        if (active) {

                            button.classList.add(
                                    "active"
                                    );
                        }


                        button.disabled =
                                disabled;


                        button.addEventListener(
                                "click",
                                function () {

                                    if (disabled) {
                                        return;
                                    }


                                    currentPage =
                                            page;


                                    render();
                                }
                        );


                        return button;
                    }


                    function addDots() {

                        var dots =
                                document.createElement(
                                        "span"
                                        );


                        dots.className =
                                "pagination-dots";


                        dots.textContent =
                                "...";


                        controls.appendChild(
                                dots
                                );
                    }


                    function renderButtons() {

                        controls.innerHTML =
                                "";


                        var pages =
                                totalPages();


                        /*
                         * Trước
                         */
                        controls.appendChild(
                                createButton(
                                        "Tr\u01B0\u1EDBc",
                                        currentPage - 1,
                                        false,
                                        currentPage === 1
                                        )
                                );


                        if (pages <= 7) {

                            for (
                                    var i = 1;
                                    i <= pages;
                                    i++
                                    ) {

                                controls.appendChild(
                                        createButton(
                                                String(i),
                                                i,
                                                i === currentPage,
                                                false
                                                )
                                        );
                            }

                        } else {

                            controls.appendChild(
                                    createButton(
                                            "1",
                                            1,
                                            currentPage === 1,
                                            false
                                            )
                                    );


                            var start =
                                    Math.max(
                                            2,
                                            currentPage - 1
                                            );


                            var end =
                                    Math.min(
                                            pages - 1,
                                            currentPage + 1
                                            );


                            if (currentPage <= 3) {

                                start = 2;
                                end = 4;
                            }


                            if (
                                    currentPage
                                    >= pages - 2
                                    ) {

                                start =
                                        pages - 3;

                                end =
                                        pages - 1;
                            }


                            if (start > 2) {

                                addDots();
                            }


                            for (
                                    var page = start;
                                    page <= end;
                                    page++
                                    ) {

                                controls.appendChild(
                                        createButton(
                                                String(page),
                                                page,
                                                page === currentPage,
                                                false
                                                )
                                        );
                            }


                            if (
                                    end
                                    < pages - 1
                                    ) {

                                addDots();
                            }


                            controls.appendChild(
                                    createButton(
                                            String(pages),
                                            pages,
                                            currentPage === pages,
                                            false
                                            )
                                    );
                        }


                        /*
                         * Sau
                         */
                        controls.appendChild(
                                createButton(
                                        "Sau",
                                        currentPage + 1,
                                        false,
                                        currentPage === pages
                                        )
                                );
                    }


                    function render() {

                        var pages =
                                totalPages();


                        if (
                                currentPage
                                > pages
                                ) {

                            currentPage =
                                    pages;
                        }


                        var startIndex =
                                (currentPage - 1)
                                * pageSize;


                        var endIndex =
                                Math.min(
                                        startIndex
                                        + pageSize,
                                        dataRows.length
                                        );


                        dataRows.forEach(
                                function (row, index) {

                                    row.style.display =
                                            index >= startIndex
                                            && index < endIndex
                                            ? ""
                                            : "none";
                                }
                        );


                        info.textContent =
                                (startIndex + 1)
                                + " - "
                                + endIndex
                                + " / "
                                + dataRows.length;


                        pagination.style.display =
                                dataRows.length > pageSize
                                ? "flex"
                                : "none";


                        renderButtons();
                    }


                    sizeSelect.addEventListener(
                            "change",
                            function () {

                                pageSize =
                                        parseInt(
                                                sizeSelect.value,
                                                10
                                                );


                                currentPage =
                                        1;


                                render();
                            }
                    );


                    render();
                }
        );

    })();

</script>