document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('/users/userData');
        const userData = await response.json();
        
        document.getElementById('email').value = userData.email;
        document.getElementById('firstName').value = userData.firstName;
        document.getElementById('lastName').value = userData.lastName;
        document.getElementById('phone').value = userData?.phoneNumber || 'Не указано';
        document.getElementById('company').value = userData?.company || 'Не указано';
        document.getElementById('department').value = userData?.department || 'Не указано';
        document.getElementById('subDepartment').value = userData?.subDepartment || 'Не указано';

        const roles = userData.role || [];
        const roleDescriptions = roles.map(role => role.description).join(', ');
        document.getElementById('role').value = roleDescriptions || 'Не указано';
    } catch (error) {
        showMessage('Ошибка загрузки данных', false);
    }
});

document.getElementById('profileForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const updateData = new Map();
    updateData.set("firstName", document.getElementById('firstName').value);
    updateData.set("lastName", document.getElementById('lastName').value);
    updateData.set("phoneNumber", document.getElementById('phone').value);

    try {
        const response = await fetch('/users/userData/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(Object.fromEntries(updateData))
        });

        if (response.ok) {
            showMessage('Данные успешно обновлены', true);
            
            const updatedResponse = await fetch('/users/userData');
            const updatedUserData = await updatedResponse.json();
            
            document.getElementById('firstName').value = updatedUserData.firstName;
            document.getElementById('lastName').value = updatedUserData.lastName;
            document.getElementById('phone').value = updatedUserData?.phoneNumber || 'Не указано';
        } else {
            showMessage('Ошибка обновления данных', false);
        }
    } catch (error) {
        console.error('Ошибка:', error);
        showMessage('Ошибка обновления данных', false);
    }
});

function showMessage(message, isSuccess) {
    const statusElement = document.getElementById('statusMessage');
    statusElement.textContent = message;
    statusElement.className = `status-message ${isSuccess ? 'success' : 'error'}`;
    statusElement.style.display = 'block';
    
    setTimeout(() => {
        statusElement.style.display = 'none';
    }, 3000);
}