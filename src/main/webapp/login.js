document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("loginForm");
    const errorText = document.getElementById("error");
    document.getElementById("loginForm").addEventListener("submit", async function(e) {
        e.preventDefault();

        const cccd = document.getElementById("studentId").value.trim();
        const password = document.getElementById("password").value.trim();

        try {
            const response = await fetch("http://localhost:8080/thisinh/auth", {
                method: "POST",
                headers: {
                    "Content-type": "application/json"
                },
                body: JSON.stringify({
                    cccd,
                    password
                })
            })

            const result = await response.text();

            if (response.ok && result === "OK") {
                window.location.href = "main.html";
            } else {
                document.getElementById("error").style.display = "block";
            }
        } catch(error) {
            console.error("Login error:", error);
        }
    });
});

const passwordInput = document.getElementById("password");
const showPasswordCheckbox = document.getElementById("showPassword");

showPasswordCheckbox.addEventListener("change", function () {
    passwordInput.type = this.checked ? "text" : "password";
});