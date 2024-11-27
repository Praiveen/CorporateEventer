document.getElementById('createCompanyForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const companyName = document.getElementById('companyName').value;
    const address = document.getElementById('address').value;

    if (!companyName || !address) {
        alert('Пожалуйста, заполните все поля.');
        return;
    }

    const companyData = {
        companyName: companyName,
        address: address,
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
            throw new Error('Ошибка при создании компании');
        }
        window.location.href = '/dashboard';
    })
    .catch(error => {
        console.error('Ошибка:', error);
        alert(error.message);
    });
});