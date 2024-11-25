import { createSearchableSelect } from './searchInSelection.js';
import { SubDepartmentManager } from './subDepartmentManager.js';

export class DepartmentManager {
    /*
     * Конструктор менеджера отделов
     */
    constructor() {
        this.form = document.getElementById('createDepartmentForm');
        this.departmentHeadSelect = document.getElementById('departmentHead');
        this.departmentsContainer = document.querySelector('.departments-container');
        this.users = [];

        this.modal = document.getElementById('modalWindow');
        this.modalTitle = this.modal.querySelector('.modal-title');
        this.modalBody = this.modal.querySelector('.modal-body');
        this.closeModalBtn = this.modal.querySelector('.close-modal');

        this.searchableSelect = null;
        this.subDepartmentManager = null;
        
        this.initialize();
        this.setupModalEvents();
    }

    /*
     * Инициализация менеджера отделов
     */
    async initialize() {
        await this.loadUsers();
        await this.loadDepartments();
        this.setupSearchableSelect();
        this.setupEventListeners();
    }

    /*
     * Загрузка отделов
     */
    async loadDepartments() {
        try {
            const response = await fetch('/dashboard/departments');
            const departments = await response.json();
            this.renderDepartments(departments);
        } catch (error) {
            console.error('Ошибка при загрузке отделов:', error);
        }
    }

    /*
     * Загрузка пользователей
     */
    async loadUsers() {
        try {
            const response = await fetch('/dashboard/company/users');
            this.users = await response.json();
            this.updateUsersList(this.users);
        } catch (error) {
            console.error('Ошибка при загрузке пользователей:', error);
        }
    }

    /*
     * Обновление списка пользователей
     */
    async updateUsersList() {
        try {
            const response = await fetch('/dashboard/company/users');
            this.users = await response.json();
            this.updateUsersList(this.users);
            if (this.searchableSelect) {
                this.searchableSelect.update(this.users);
            }
        } catch (error) {
            console.error('Ошибка при загрузке пользователей:', error);
        }
    }

    /*
     * Настройка селекта руководителя
     */
    setupSearchableSelect() {
        this.searchableSelect = createSearchableSelect(
            this.departmentHeadSelect, 
            this.users,
            {
                placeholder: 'Выберите руководителя',
                searchPlaceholder: 'Поиск сотрудника...'
            }
        );
    }
    
    /*
     * Обновление списка пользователей
     */
    updateOptionsContainer(container, users) {
        container.innerHTML = '';
        if (users.length === 0) {
            const noResults = document.createElement('div');
            noResults.className = 'select-option no-results';
            noResults.textContent = 'Нет результатов';
            container.appendChild(noResults);
            return;
        }
        
        users.forEach(user => {
            const option = document.createElement('div');
            option.className = 'select-option';
            option.textContent = `${user.firstName} ${user.lastName}`;
            option.dataset.value = user.userId;
            
            option.addEventListener('click', () => {
                this.departmentHeadSelect.value = user.userId;
                const selectButton = container.parentNode.previousElementSibling;
                selectButton.querySelector('.selected-value').textContent = 
                    `${user.firstName} ${user.lastName}`;
                container.parentNode.classList.remove('show');
                selectButton.classList.remove('active');
            });
            
            container.appendChild(option);
        });
    }

    /*
     * Обновление списка пользователей
     */
    updateUsersList(users) {
        this.departmentHeadSelect.innerHTML = '<option value="">Выберите руководителя</option>';
        users.forEach(user => {
            const option = document.createElement('option');
            option.value = user.userId;
            option.textContent = `${user.firstName} ${user.lastName}`;
            this.departmentHeadSelect.appendChild(option);
        });
    }

    /*
     * Настройка событий формы создания отдела
     */
    setupEventListeners() {
        this.form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const departmentData = {
                departmentName: document.getElementById('departmentName').value,
                headId: this.departmentHeadSelect.value
            };

            try {
                const response = await fetch('/dashboard/departments/create', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(departmentData)
                });

                if (response.ok) {
                    alert('Отдел успешно создан');
                    await this.loadDepartments();
                    await this.loadUsers();
                    this.form.reset();

                    if (this.searchableSelect) {
                        this.searchableSelect.setValue('', 'Выберите руководителя');
                    }
                } else {
                    const error = await response.text();
                    alert(`Ошибка при создании отдела: ${error}`);
                }
            } catch (error) {
                console.error('Ошибка при создании отдела:', error);
                alert('Произошла ошибка при создании отдела');
            }
        });
    }

    /*
     * Рендер отделов
     */
    renderDepartments(departments) {
        this.departmentsContainer.innerHTML = departments.length ? '' : 
            '<div class="no-departments">Отделы пока не созданы</div>';

        departments.forEach(department => {
            const departmentElement = document.createElement('div');
            departmentElement.className = 'department-card';
            departmentElement.innerHTML = `
                <div class="department-header">
                    <h4 class="department-name">${department.name}</h4>
                    <div class="department-actions">
                    <button class="btn-subdepartments" data-id="${department.id}">
                        <i class="fas fa-sitemap"></i>
                    </button>
                    <button class="btn-edit" data-id="${department.id}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-delete" data-id="${department.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                    </div>
                </div>
                <div class="department-info">
                    <div class="manager-info">
                        <span class="label">Руководитель:</span>
                        <span class="value">${department.managerName}</span>
                    </div>
                    <div class="employees-count">
                        <span class="label">Сотрудников:</span>
                        <span class="value">${department.employeesCount}</span>
                    </div>
                </div>
            `;

            const subdepartmentsBtn = departmentElement.querySelector('.btn-subdepartments');
            const editBtn = departmentElement.querySelector('.btn-edit');
            const deleteBtn = departmentElement.querySelector('.btn-delete');

            subdepartmentsBtn.addEventListener('click', () => this.showSubDepartments(department.id));
            editBtn.addEventListener('click', () => this.editDepartment(department.id));
            deleteBtn.addEventListener('click', () => this.deleteDepartment(department.id));

            this.departmentsContainer.appendChild(departmentElement);
        });
    }

    /*
     * Показ подотделов
     */
    showSubDepartments(departmentId) {
        const createSubdepartmentBlock = document.querySelector('.create-subdepartment-block');
        const subdepartmentsListBlock = document.querySelector('.subdepartments-list-block');
        
        if (this.subDepartmentManager && this.subDepartmentManager.parentDepartmentId === departmentId) {
            createSubdepartmentBlock.style.display = 'none';
            subdepartmentsListBlock.style.display = 'none';
            this.subDepartmentManager.destroy();
            this.subDepartmentManager = null;
            return;
        }
        
        createSubdepartmentBlock.style.display = 'block';
        subdepartmentsListBlock.style.display = 'block';
        
        if (this.subDepartmentManager) {
            this.subDepartmentManager.destroy();
        }
        this.subDepartmentManager = new SubDepartmentManager(departmentId);
    }
    

    /*
     * Редактирование отдела
     */
    async editDepartment(departmentId) {
        try {
            const [department, users] = await Promise.all([
                fetch(`/dashboard/departments/getdepatmentdata/${departmentId}`).then(r => r.json()),
                fetch('/dashboard/company/users').then(r => r.json())
            ]);

            // Добавляем текущего руководителя в список, если его там нет
            const currentManager = users.find(u => u.userId === department.managerId);
            if (!currentManager) {
                users.push({
                    userId: department.managerId,
                    firstName: department.managerName.split(' ')[0],
                    lastName: department.managerName.split(' ')[1]
                });
            }

            const formHtml = `
                <form id="editDepartmentForm" class="department-form">
                    <input type="hidden" id="editDepartmentId" value="${department.id}">
                    <div class="form-group">
                        <label for="editDepartmentName">Название отдела:</label>
                        <input 
                            type="text" 
                            id="editDepartmentName" 
                            name="departmentName" 
                            value="${department.name}"
                            required 
                            class="form-input"
                        >
                    </div>
                    <div class="form-group">
                        <label for="editDepartmentHead">Руководитель отдела:</label>
                        <select 
                            id="editDepartmentHead" 
                            name="departmentHead" 
                            required 
                            class="form-select"
                        >
                        </select>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn-primary">
                            <i class="fas fa-save"></i> Сохранить
                        </button>
                    </div>
                </form>
            `;

            // Настраиваем модальное окно
            this.modalTitle.textContent = 'Редактирование отдела';
            this.modalBody.innerHTML = formHtml;

            // Создаем поисковый селект для выбора руководителя
            const editSelect = this.modalBody.querySelector('#editDepartmentHead');
            createSearchableSelect(
                editSelect,
                users,
                {
                    getDisplayText: (user) => `${user.firstName} ${user.lastName}`,
                    getValue: (user) => user.userId,
                    placeholder: 'Выберите руководителя',
                    searchPlaceholder: 'Поиск сотрудника...'
                }
            );

            // Устанавливаем текущего руководителя
            editSelect.value = department.managerId;
            const selectWrapper = editSelect.nextElementSibling;
            if (selectWrapper) {
                const selectedValue = selectWrapper.querySelector('.selected-value');
                if (selectedValue) {
                    selectedValue.textContent = `${department.managerName}`;
                }
            }

            // Добавляем обработчик отправки формы
            const form = this.modalBody.querySelector('#editDepartmentForm');
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.updateDepartment();
            });

            this.openModal();
        } catch (error) {
            console.error('Ошибка при загрузке данных отдела:', error);
            alert('Не удалось загрузить данные отдела');
        }
    }

    /*
     * Генерация опций для селекта руководителя
     */
    generateUserOptions(users, selectedUserId) {
        return users.map(user => `
            <option 
                value="${user.userId}" 
                ${user.userId === selectedUserId ? 'selected' : ''}
            >
                ${user.firstName} ${user.lastName}
            </option>
        `).join('');
    }

    /*
     * Обновление отдела
     */
    async updateDepartment() {
        const departmentId = document.getElementById('editDepartmentId').value;
        const departmentData = {
            departmentName: document.getElementById('editDepartmentName').value,
            headId: document.getElementById('editDepartmentHead').value
        };

        try {
            const response = await fetch(`/dashboard/departments/update/${departmentId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(departmentData)
            });

            if (response.ok) {
                await this.loadDepartments();
                this.closeModal();
            } else {
                const error = await response.text();
                alert(`Ошибка при обновлении отдела: ${error}`);
            }
        } catch (error) {
            console.error('Ошибка при обновлении отдела:', error);
            alert('Произошла ошибка при обновлении отдела');
        }
    }

    /*
     * Открытие модального окна
     */
    openModal() {
        this.modal.classList.add('show');
    }

    /*
     * Закрытие модального окна
     */
    closeModal() {
        this.modal.classList.remove('show');
        this.modalBody.innerHTML = '';
        this.modalTitle.textContent = '';
    }

    /*
     * Настройка событий модального окна
     */
    setupModalEvents() {
        this.closeModalBtn.addEventListener('click', () => this.closeModal());
        this.modal.addEventListener('click', (e) => {
            if (e.target === this.modal) this.closeModal();
        });
    }

    /*
     * Удаление отдела
     */
    async deleteDepartment(departmentId) {
        if (confirm('Вы уверены, что хотите удалить этот отдел?')) {
            try {
                const response = await fetch(`/dashboard/departments/delete/${departmentId}`, {
                    method: 'POST'
                });

                if (response.ok) {
                    await this.loadDepartments();
                    await this.loadUsers();
                } else {
                    const error = await response.text();
                    alert(`Ошибка при удалении отдела: ${error}`);
                }
            } catch (error) {
                console.error('Ошибка при удалении отдела:', error);
                alert('Произошла ошибка при удалении отдела');
            }
        }
    }
}



let departmentManager;
/*
 * Инициализация менеджера отделов
 */
document.addEventListener('DOMContentLoaded', () => {
    departmentManager = new DepartmentManager();
});