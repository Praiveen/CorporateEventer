const form = document.querySelector('.modal-form');

form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = new FormData(form);

    const data = {};
    formData.forEach((value, key) => {
        data[key] = value;
    });

    try {
        const response = await fetch('/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            document.querySelector('.alert').innerText = responseData.message; 
        } else {
            document.querySelector('.alert').innerText = responseData.message;
        }
    } catch (error) {
        console.error('Ошибка при отправке данных:', error);
        document.querySelector('.alert').innerText = 'Произошла ошибка при отправке данных';
    }
});