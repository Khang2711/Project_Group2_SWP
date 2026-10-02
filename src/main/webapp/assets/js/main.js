document.addEventListener("DOMContentLoaded", function () {

    const dateDisplay = document.getElementById("dateDisplay");
    const dateInput = document.getElementById("date");

    if (dateDisplay && dateInput) {

        /*
         * Show existing date
         */
        if (dateInput.value) {
            displayDate(dateInput.value, dateDisplay);
        }

        /*
         * Click anywhere on the visible input
         */
        dateDisplay.addEventListener("click", function () {

            if (dateInput.showPicker) {
                dateInput.showPicker();
            } else {
                dateInput.click();
            }
        });

        /*
         * When user selects a date
         */
        dateInput.addEventListener("change", function () {
            displayDate(this.value, dateDisplay);
        });
    }
});

/*
 * Display date
 *
 * Convert:2026-10-01
 * 
 * to: 01/10/2026 (DD/MM/YY)
 */

function displayDate(dateValue, dateDisplay) {

    if (!dateValue) {

        dateDisplay.value = "";

        return;
    }

    const parts = dateValue.split("-");

    if (parts.length === 3) {

        const year = parts[0];
        const month = parts[1];
        const day = parts[2];

        dateDisplay.value = day + "/" + month + "/" + year;
    }

}