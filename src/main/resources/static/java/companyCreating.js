document.getElementById('createCompanyForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const companyData = {
        companyName: document.getElementById('companyName').value,
        address: document.getElementById('address').value,
        users: [],
        departments: []
    };

    fetch('/dashboard/starter/createCompany/newcompany', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(companyData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || 'Ошибка при создании компании');
            });
        }
        return response.json();
    })
    .then(data => {
        console.log('Успех:', data);
        window.location.href = '/dashboard';
    })
    .catch(error => {
        console.error('Ошибка:', error);
        alert(error.message);
    });
});