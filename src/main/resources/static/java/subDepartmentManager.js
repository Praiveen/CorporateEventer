import { createSearchableSelect } from './searchInSelection.js';

export class SubDepartmentManager {
    constructor(parentDepartmentId = null) {
        console.log('SubDepartmentManager constructor called');
        
        this.parentDepartmentId = parentDepartmentId;
        this.form = document.getElementById('createSubDepartmentForm');
        this.subDepartmentHeadSelect = document.getElementById('subDepartmentHead');
        this.subdepartmentsContainer = document.querySelector('.subdepartments-container');
        this.users = [];
        this.submitHandler = null;

        if (window.userRoles.includes('DEPARTMENT_MANAGER')) {
            // Для менеджера отдела показываем блоки сразу
            this.loadManagerDepartment();
        } else if (parentDepartmentId) {
            // Для директора инициализируем с переданным ID отдела
            this.initialize();
        }
    }

    setupForDirector() {
        console.log('Setting up for Director');
        const createBlock = document.querySelector('.create-subdepartment-block');
        const listBlock = document.querySelector('.subdepartments-list-block');
        if (createBlock) createBlock.style.display = 'none';
        if (listBlock) listBlock.style.display = 'none';
    }

    setupForDepartmentManager() {
        console.log('Setting up for Department Manager');
        const createBlock = document.querySelector('.create-subdepartment-block');
        const listBlock = document.querySelector('.subdepartments-list-block');
        if (createBlock) createBlock.style.display = 'block';
        if (listBlock) listBlock.style.display = 'block';

        this.loadManagerDepartment();
    }

    toggleSubDepartmentBlocks() {
        const createBlock = document.querySelector('.create-subdepartment-block');
        const listBlock = document.querySelector('.subdepartments-list-block');
        if (createBlock) createBlock.style.display = createBlock.style.display === 'none' ? 'block' : 'none';
        if (listBlock) listBlock.style.display = listBlock.style.display === 'none' ? 'block' : 'none';
    }


    async loadManagerDepartment() {
        try {
            const response = await fetch('/dashboard/current-user-department');
            if (!response.ok) throw new Error('Ошибка при загрузке информации об отделе');
            const department = await response.json();
            this.parentDepartmentId = department.id;
            
            // Показываем блоки для менеджера отдела
            const createBlock = document.querySelector('.create-subdepartment-block');
            const listBlock = document.querySelector('.subdepartments-list-block');
            if (createBlock) createBlock.style.display = 'block';
            if (listBlock) listBlock.style.display = 'block';
            
            await this.initialize();
        } catch (error) {
            console.error('Ошибка при загрузке информации об отделе:', error);
        }
    }

    async initialize() {
        await this.loadUsers();
        await this.loadSubDepartments();
        this.setupEventListeners();
    }


    setupSearchableSelect() {
        if (this.searchableSelect) {
            const oldWrapper = this.subDepartmentHeadSelect.nextElementSibling;
            if (oldWrapper && oldWrapper.classList.contains('custom-select-wrapper')) {
                oldWrapper.remove();
            }
        }
    
        this.searchableSelect = createSearchableSelect(
            this.subDepartmentHeadSelect,
            this.users,
            {
                getDisplayText: (user) => `${user.firstName} ${user.lastName}`,
                getValue: (user) => user.userId,
                placeholder: 'Выберите руководителя подотдела',
                searchPlaceholder: 'Поиск сотрудника...'
            }
        );
    }

    async loadUsers() {
        try {
            const response = await fetch(`/dashboard/departments/${this.parentDepartmentId}/available-managers`);
            this.users = await response.json();
            this.updateUsersList(this.users);
    
            if (this.searchableSelect) {
                this.searchableSelect.update(this.users);
            }
        } catch (error) {
            console.error('Ошибка при загрузке пользователей:', error);
        }
    }

    // async initialize() {
    //     await this.loadUsers();
    //     this.setupSearchableSelect();
    //     await this.loadSubDepartments();
    //     this.setupEventListeners();
        
    //     document.querySelector('.create-subdepartment-block').style.display = 'block';
    //     document.querySelector('.subdepartments-list-block').style.display = 'block';
    // }

    async loadUsers() {
        try {
            const response = await fetch(`/dashboard/departments/${this.parentDepartmentId}/available-managers`);
            if (!response.ok) {
                throw new Error('Ошибка при загрузке пользователей');
            }
            this.users = await response.json();
            this.updateUsersList(this.users);
    
            if (this.searchableSelect) {
                this.searchableSelect.update(this.users);
            }
        } catch (error) {
            console.error('Ошибка при загрузке пользователей:', error);
        }
    }

    updateUsersList(users) {
        this.subDepartmentHeadSelect.innerHTML = '<option value="">Выберите руководителя</option>';
        users.forEach(user => {
            const option = document.createElement('option');
            option.value = user.userId;
            option.textContent = `${user.firstName} ${user.lastName}`;
            this.subDepartmentHeadSelect.appendChild(option);
        });
    }

    async loadSubDepartments() {
        try {
            const response = await fetch(`/dashboard/departments/${this.parentDepartmentId}/subdepartments`);
            if (!response.ok) throw new Error('Ошибка при загрузке подотделов');
            
            const subdepartments = await response.json();
            this.renderSubDepartments(subdepartments);
        } catch (error) {
            console.error('Ошибка при загрузке подотделов:', error);
            this.subdepartmentsContainer.innerHTML = '<div class="error-message">Ошибка при загрузке подотделов</div>';
        }
    }

    setupEventListeners() {
        this.submitHandler = async (e) => {
            e.preventDefault();
            
            const headId = this.subDepartmentHeadSelect.value;
            console.log('Form submission - headId:', headId);
            console.log('Select element:', this.subDepartmentHeadSelect);
            console.log('Select options:', this.subDepartmentHeadSelect.innerHTML);
            
            if (!headId || headId === '') {
                console.log('No head selected');
                alert('Пожалуйста, выберите руководителя подотдела');
                return;
            }
            
            const subdepartmentData = {
                subdepartmentName: document.getElementById('subdepartmentName').value,
                headId: parseInt(headId),
                departmentId: this.parentDepartmentId
            };
            
            console.log('Submitting data:', subdepartmentData);
            
            try {
                const response = await fetch('/dashboard/departments/subdepartments/create', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(subdepartmentData)
                });

                if (response.ok) {
                    alert('Подотдел успешно создан');

                    await this.loadUsers();
                    if (window.departmentManager) {
                        await window.departmentManager.loadUsers();
                    }

                    await this.loadSubDepartments();
                    this.form.reset();
                    
                    if (this.searchableSelect) {
                        this.searchableSelect.setValue('', 'Выберите руководителя');
                    }
                } else {
                    const error = await response.text();
                    alert(`Ошибка при создании подотдела: ${error}`);
                }
            } catch (error) {
                console.error('Ошибка при создании подотдела:', error);
                alert('Произошла ошибка при создании подотдела');
            }
        };
        
        this.form.addEventListener('submit', this.submitHandler);
    }

    renderSubDepartments(subdepartments) {
        this.subdepartmentsContainer.innerHTML = subdepartments.length ? '' :
            '<div class="no-subdepartments">Подотделы пока не созданы</div>';

        subdepartments.forEach(subdepartment => {
            const subdepartmentElement = document.createElement('div');
            subdepartmentElement.className = 'department-card';
            subdepartmentElement.innerHTML = `
                <div class="department-header">
                    <h4 class="department-name">${subdepartment.name}</h4>
                    <div class="department-actions">
                        <button class="btn-manage-employees" data-id="${subdepartment.id}" title="Управление сотрудниками">
                            <i class="fas fa-users"></i>
                        </button>
                        <button class="btn-edit" data-id="${subdepartment.id}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn-delete" data-id="${subdepartment.id}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
                <div class="department-info">
                    <div class="manager-info">
                        <span class="label">Руководитель:</span>
                        <span class="value">${subdepartment.managerName}</span>
                    </div>
                    <div class="employees-count">
                        <span class="label">Сотрудников:</span>
                        <span class="value">${subdepartment.employeesCount}</span>
                    </div>
                </div>
            `;

            const editBtn = subdepartmentElement.querySelector('.btn-edit');
            const deleteBtn = subdepartmentElement.querySelector('.btn-delete');
            const manageEmployeesBtn = subdepartmentElement.querySelector('.btn-manage-employees');
            manageEmployeesBtn.addEventListener('click', () => {
                const employeesBlock = document.querySelector('.subdepartment-employees-block');
                if (employeesBlock.style.display === 'block') {
                    employeesBlock.style.display = 'none';
                    window.employeeManager.setCurrentSubdepartment(null);
                } else {
                    employeesBlock.style.display = 'block';
                    window.employeeManager.setCurrentSubdepartment(subdepartment.id);
                }
            });

            editBtn.addEventListener('click', () => this.editSubDepartment(subdepartment.id));
            deleteBtn.addEventListener('click', () => this.deleteSubDepartment(subdepartment.id));

            this.subdepartmentsContainer.appendChild(subdepartmentElement);
        });
    }

    async editSubDepartment(subdepartmentId) {
        try {
            const [subdepartmentResponse, usersResponse] = await Promise.all([
                fetch(`/dashboard/departments/subdepartments/${subdepartmentId}`),
                fetch('/dashboard/company/users')
            ]);
    
            const subdepartment = await subdepartmentResponse.json();
            const users = await usersResponse.json();
            const modal = document.getElementById('modalWindow');
            const modalTitle = modal.querySelector('.modal-title');
            const modalBody = modal.querySelector('.modal-body');

            modalTitle.textContent = 'Редактирование подотдела';
            modalBody.innerHTML = `
                <form id="editSubDepartmentForm" class="edit-form">
                    <input type="hidden" id="editSubDepartmentId" value="${subdepartmentId}">
                    <div class="form-group">
                        <label for="editSubDepartmentName">Название подотдела:</label>
                        <input type="text" id="editSubDepartmentName" class="form-control" 
                               value="${subdepartment.name}" required>
                    </div>
                    <div class="form-group">
                        <label for="editSubDepartmentHead">Руководитель подотдела:</label>
                        <select id="editSubDepartmentHead" class="form-select" required>
                            <option value="">Выберите руководителя</option>
                        </select>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Сохранить</button>
                    </div>
                </form>
            `;
            const editSelect = modalBody.querySelector('#editSubDepartmentHead');
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
            editSelect.value = subdepartment.managerId;
            const selectWrapper = editSelect.nextElementSibling;
            if (selectWrapper) {
                const selectedValue = selectWrapper.querySelector('.selected-value');
                if (selectedValue) {
                    selectedValue.textContent = subdepartment.managerName;
                }
            }
            const form = modalBody.querySelector('#editSubDepartmentForm');
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                
                const updatedData = {
                    subdepartmentName: document.getElementById('editSubDepartmentName').value,
                    headId: parseInt(document.getElementById('editSubDepartmentHead').value),
                    departmentId: this.parentDepartmentId
                };
    
                try {
                    const response = await fetch(`/dashboard/departments/subdepartments/update/${subdepartmentId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(updatedData)
                    });
    
                    if (response.ok) {
                        await this.loadSubDepartments();
                        await this.loadUsers();
                        if (window.departmentManager) {
                            await window.departmentManager.loadUsers();
                        }
                        modal.classList.remove('show');
                        modalBody.innerHTML = '';
                    } else {
                        const error = await response.text();
                        alert(`Ошибка при обновлении подотдела: ${error}`);
                    }
                } catch (error) {
                    console.error('Ошибка при обновлении подотдела:', error);
                    alert('Произошла ошибка при обновлении подотдела');
                }
            });
            modal.classList.add('show');
    
        } catch (error) {
            console.error('Ошибка при загрузке данных подотдела:', error);
            alert('Не удалось загрузить данные подотдела');
        }
    }

    async deleteSubDepartment(subdepartmentId) {
        if (confirm('Вы уверены, что хотите удалить этот подотдел?')) {
            try {
                const response = await fetch(`/dashboard/departments/subdepartments/delete/${subdepartmentId}`, {
                    method: 'POST'
                });

                if (response.ok) {
                    await this.loadSubDepartments();
                    await this.loadUsers();
                } else {
                    const error = await response.text();
                    alert(`Ошибка при удалении подотдела: ${error}`);
                }
            } catch (error) {
                console.error('Ошибка при удалении подотдела:', error);
                alert('Произошла ошибка при удалении подотдела');
            }
        }
    }

    destroy() {
        document.querySelector('.create-subdepartment-block').style.display = 'none';
        document.querySelector('.subdepartments-list-block').style.display = 'none';
        
        this.subdepartmentsContainer.innerHTML = '';
        
        const subDepartmentSelectParent = this.subDepartmentHeadSelect.parentElement;
        
        const selectWrappers = subDepartmentSelectParent.querySelectorAll('.custom-select-wrapper');
        selectWrappers.forEach(wrapper => {
            if (wrapper.parentElement) {
                wrapper.parentElement.removeChild(wrapper);
            }
        });
        
        if (this.subDepartmentHeadSelect) {
            this.subDepartmentHeadSelect.style.display = '';
            this.subDepartmentHeadSelect.innerHTML = '<option value="">Выберите руководителя</option>';
        }
        
        if (this.searchableSelect) {
            this.searchableSelect = null;
        }
        
        if (this.form && this.submitHandler) {
            this.form.removeEventListener('submit', this.submitHandler);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const userRoles = window.userRoles;
    
    if (userRoles.includes('DIRECTOR') || userRoles.includes('DEPARTMENT_MANAGER')) {
        window.subDepartmentManager = new SubDepartmentManager();
    }
});