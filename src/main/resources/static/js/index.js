document.addEventListener('DOMContentLoaded', () => {

    // Tab Switching Logic
    const navBtns = document.querySelectorAll('.nav-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    navBtns.forEach(btn => {
        btn.addEventListener('click', () => {

            navBtns.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));

            btn.classList.add('active');

            const targetId = btn.getAttribute('data-target');

            document.getElementById(targetId)
                ?.classList.add('active');
        });
    });

    const togglePassword = document.getElementById("togglePassword");
    const password = document.getElementById("password");

    const eyeOpen = document.getElementById("eye-open");
    const eyeClosed = document.getElementById("eye-closed");

    if (togglePassword && password) {

        togglePassword.addEventListener("click", () => {

            const isPassword =
                password.getAttribute("type") === "password";

            if (isPassword) {

                password.setAttribute("type", "text");

                eyeOpen.style.display = "none";
                eyeClosed.style.display = "block";

                togglePassword.style.color = "#3b82f6";

            } else {

                password.setAttribute("type", "password");

                eyeOpen.style.display = "block";
                eyeClosed.style.display = "none";

                togglePassword.style.color = "#6b7280";
            }
        });
    }

    // Calc Method Switching
    const methodRadios = document.querySelectorAll('input[name="calc-method"]');

    const formDgnl = document.getElementById('form-dgnl');

    const formVsat = document.getElementById('form-vsat');
    
    const dgnlResult = document.getElementById('dgnl-result');

    const vsatResult = document.getElementById('vsat-result')

    methodRadios.forEach(radio => {

        radio.addEventListener('change', (e) => {

            if (e.target.value === 'dgnl') {

                formDgnl.classList.remove('hidden');

                formVsat.classList.add('hidden');

                vsatResult?.classList.add('hidden');
        
                dgnlResult?.classList.remove('hidden');

            } else {

                formVsat.classList.remove('hidden');
                
                formDgnl.classList.add('hidden');
        
                dgnlResult?.classList.add('hidden')
        
                vsatResult?.classList.remove('hidden');

            }
        });
    });

    // Current tabs
    const body = document.body;

    const activeTab = body.dataset.activeTab || 'tracuu';
                           
    const activeMethod = body.dataset.activeMethod || 'dgnl';

    document.querySelectorAll('.tab-pane')
        .forEach(p => p.classList.remove('active'));

    document.querySelectorAll('.nav-btn')
        .forEach(btn => btn.classList.remove('active'));

    document.getElementById(`tab-${activeTab}`)
        ?.classList.add('active');

    document.querySelector(`[data-target="tab-${activeTab}"]`)
        ?.classList.add('active');

    if (activeMethod === 'vsat') {

        formVsat.classList.remove('hidden');

        formDgnl.classList.add('hidden');
        
        dgnlResult?.classList.add('hidden');
        
        vsatResult?.classList.remove('hidden');

        document.querySelector('input[value="vsat"]').checked = true;

    } else {

        formDgnl.classList.remove('hidden');

        formVsat.classList.add('hidden');
        
        vsatResult?.classList.add('hidden');
        
        dgnlResult?.classList.remove('hidden');

        document.querySelector('input[value="dgnl"]').checked = true;
    }
});