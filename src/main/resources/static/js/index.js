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
    
    const formThpt = document.getElementById('form-thpt');
    
    const dgnlResult = document.getElementById('dgnl-result');

    const vsatResult = document.getElementById('vsat-result')
    
    const thptResult = document.getElementById('thpt-result')

    methodRadios.forEach(radio => {

        radio.addEventListener('change', (e) => {

            if (e.target.value === 'dgnl') {

                formDgnl.classList.remove('hidden');

                formVsat.classList.add('hidden');
                
                formThpt.classList.add('hidden');

                dgnlResult?.classList.remove('hidden');
                
                vsatResult?.classList.add('hidden');

                thptResult?.classList.add('hidden');

            } else if (e.target.value === "vsat") {

                formDgnl.classList.add('hidden');

                formVsat.classList.remove('hidden');
                
                formThpt.classList.add('hidden');

                dgnlResult?.classList.add('hidden');
                
                vsatResult?.classList.remove('hidden');

                thptResult?.classList.add('hidden');

            } else {

                formDgnl.classList.add('hidden');

                formVsat.classList.add('hidden');
                
                formThpt.classList.remove('hidden');

                dgnlResult?.classList.add('hidden');
                
                vsatResult?.classList.add('hidden');

                thptResult?.classList.remove('hidden');

            }
        });
    });

    // SUBJECT CHECKBOX LIMIT
    const subjectChecks = document.querySelectorAll('.subject-check');

    subjectChecks.forEach(check => {

        check.addEventListener('change', (e) => {

            const checkedCount =
                document.querySelectorAll('.subject-check:checked').length;

            const targetId = e.target.dataset.target;

            const input =
                document.getElementById(targetId);

            // limit max 4
            if (checkedCount > 4) {

                e.target.checked = false;

                alert('Chỉ được chọn tối đa 4 môn.');

                return;
            }

            // enable / disable input
            if (e.target.checked) {

                input.disabled = false;

            } else {

                input.disabled = true;
                
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

        formDgnl.classList.add('hidden');

        formVsat.classList.remove('hidden');
        
        formThpt.classList.add('hidden');

        dgnlResult?.classList.add('hidden');
        
        vsatResult?.classList.remove('hidden');

        thptResult?.classList.add('hidden');

        document.querySelector('input[value="vsat"]').checked = true;

    } else if (activeMethod === "dgnl") {

        formDgnl.classList.remove('hidden');

        formVsat.classList.add('hidden');
        
        formThpt.classList.add('hidden');

        dgnlResult?.classList.remove('hidden');
        
        vsatResult?.classList.add('hidden');

        thptResult?.classList.add('hidden');

        document.querySelector('input[value="dgnl"]').checked = true;
    } else {

        formDgnl.classList.add('hidden');

        formVsat.classList.add('hidden');
        
        formThpt.classList.remove('hidden');

        dgnlResult?.classList.add('hidden');
        
        vsatResult?.classList.add('hidden');

        thptResult?.classList.remove('hidden');

        document.querySelector('input[value="thpt"]').checked = true;

    }
});