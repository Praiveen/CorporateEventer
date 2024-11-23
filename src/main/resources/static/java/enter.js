document.querySelector('.modal-form').addEventListener('submit', function(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    fetch('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка входа');
        }
        return response.json();
    })
    .then(data => {
        console.log('Успех:', data);
        window.location.href = '/dashboard';
    })
    .catch((error) => {
        console.error('Ошибка:', error);
        alert('Неверная почта или пароль!');
    });
});