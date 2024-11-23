// Получаем элемент формы
const form = document.querySelector('.modal-form');

// Добавляем обработчик события на отправку формы
form.addEventListener('submit', async (event) => {
    event.preventDefault(); // Предотвращаем стандартное поведение формы

    // Создаем объект FormData для сбора данных формы
    const formData = new FormData(form);

    // Преобразуем FormData в JSON
    const data = {};
    formData.forEach((value, key) => {
        data[key] = value;
    });

    try {
        // Отправляем данные на сервер
        const response = await fetch('/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        // Обрабатываем ответ
        if (!response.ok) {
            
            const errorMessage = await response.text();
            console.log(errorMessage);
            document.querySelector('.alert').innerText = errorMessage;
        } 
        else {
            const goodRegist = await response.text();
            console.log(goodRegist);
            document.querySelector('.alert').innerText = goodRegist;
            // window.location.href = '/login'; // Перенаправление на страницу входа
        }
    } catch (error) {
        console.error('Ошибка при отправке данных:', error);
        document.querySelector('.alert').innerText = 'Произошла ошибка при отправке данных.';
    }
});