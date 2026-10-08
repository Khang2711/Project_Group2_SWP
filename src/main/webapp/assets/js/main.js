document.addEventListener("DOMContentLoaded", function () {

    /*
     * =========================
     * DATE PICKER MOVIE LIST
     * =========================
     */

    const dateDisplay = document.getElementById("dateDisplay");
    const dateInput = document.getElementById("date");

    if (dateDisplay && dateInput) {

        if (dateInput.value) {
            displayDate(dateInput.value, dateDisplay);
        }

        dateDisplay.addEventListener("click", function () {

            if (dateInput.showPicker) {
                dateInput.showPicker();
            } else {
                dateInput.click();
            }
        });

        dateInput.addEventListener("change", function () {
            displayDate(this.value, dateDisplay);
        });
    }


    /*
     * =========================
     * HOME CAROUSEL
     * =========================
     */

    const carousel = document.querySelector(".carousel-container");

    if (carousel) {

        const track = document.getElementById("carouselTrack");
        const slides = carousel.querySelectorAll(".carousel-slide");
        const previousButton = document.getElementById("carouselPrev");
        const nextButton = document.getElementById("carouselNext");
        const dotsContainer = document.getElementById("carouselDots");

        let currentIndex = 0;
        let dots = [];


        /*
         * Create dots
         */
        for (let i = 0; i < slides.length; i++) {

            const dot = document.createElement("button");

            dot.type = "button";
            dot.className = "carousel-dot";

            dot.addEventListener("click", function () {
                showSlide(i);
            });

            dotsContainer.appendChild(dot);

            dots.push(dot);
        }


        /*
         * Show slide
         */
        function showSlide(index) {

            if (slides.length === 0) {
                return;
            }

            if (index >= slides.length) {
                index = 0;
            }

            if (index < 0) {
                index = slides.length - 1;
            }

            track.style.transform =
                    "translateX(-" + (index * 100) + "%)";

            for (let i = 0; i < dots.length; i++) {
                dots[i].classList.remove("active");
            }

            if (dots[index]) {
                dots[index].classList.add("active");
            }

            currentIndex = index;
        }


        /*
         * First slide
         */
        showSlide(0);


        /*
         * Next
         */
        if (nextButton) {

            nextButton.addEventListener("click", function () {

                showSlide(currentIndex + 1);
            });
        }


        /*
         * Previous
         */
        if (previousButton) {

            previousButton.addEventListener("click", function () {

                showSlide(currentIndex - 1);
            });
        }


        /*
         * Auto slide every 3 seconds
         */
        if (slides.length > 1) {

            setInterval(function () {

                showSlide(currentIndex + 1);

            }, 3000);
        }
    }
});


/*
 * Convert yyyy-MM-dd
 * to dd/MM/yyyy
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