document.querySelector('.modal-form').addEventListener('submit', function(event) {
    event.preventDefault(); // Отменяем стандартное поведение формы

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    fetch('/auth/login', { // Убедитесь, что путь соответствует вашему контроллеру
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }), // Отправляем данные в формате JSON
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка входа');
        }
        return response.json();
    })
    .then(data => {
        // Обработка успешного входа, например, перенаправление
        // localStorage.setItem('token', data.token); // Предполагается, что токен возвращается в поле 'token'
        console.log('Успех:', data);
        // window.location.href = '/dashboard'; // Пример перенаправления
    })
    .catch((error) => {
        // Обработка ошибок
        console.error('Ошибка:', error);
        alert('Неверная почта или пароль!');
    });
});