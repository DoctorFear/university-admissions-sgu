document.addEventListener("DOMContentLoaded", () => {
    // 1. Tab switching
    const navBtns = document.querySelectorAll(".nav-btn");
    const tabPanes = document.querySelectorAll(".tab-pane");

    navBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-target");

            navBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            tabPanes.forEach(pane => {
                if (pane.id === target) {
                    pane.classList.add("active");
                } else {
                    pane.classList.remove("active");
                }
            });
        });
    });

    // 2. Toggle Password Visibility
    const togglePassword = document.getElementById("togglePassword");
    const password = document.getElementById("password");
    if (togglePassword && password) {
        togglePassword.addEventListener("click", () => {
            const type = password.getAttribute("type") === "password" ? "text" : "password";
            password.setAttribute("type", type);

            // Toggle eye styling if needed (using svg stroke)
            const eyeIcon = togglePassword.querySelector("svg");
            if (type === "text") {
                togglePassword.style.color = "#3b82f6";
            } else {
                togglePassword.style.color = "#6b7280";
            }
        });
    }

    // 3. Calc method switching
    const calcRadios = document.querySelectorAll('input[name="calc-method"]');
    const formDgnl = document.getElementById('form-dgnl');
    const formVsat = document.getElementById('form-vsat');
    const resultDgnl = document.getElementById('result-dgnl');
    const resultVsat = document.getElementById('result-vsat');

    calcRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            if (e.target.value === 'dgnl') {
                formDgnl.classList.remove('hidden');
                formVsat.classList.add('hidden');
                if (resultDgnl) resultDgnl.classList.remove('hidden');
                if (resultVsat) resultVsat.classList.add('hidden');
            } else {
                formDgnl.classList.add('hidden');
                formVsat.classList.remove('hidden');
                if (resultDgnl) resultDgnl.classList.add('hidden');
                if (resultVsat) resultVsat.classList.remove('hidden');
            }
        });
    });
});
