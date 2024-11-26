class EmployeeManager {
    constructor() {
        this.currentDepartmentId = null;
        this.currentSubdepartmentId = null;
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        document.getElementById('addDepartmentEmployees')?.addEventListener('click', () => {
            const modal = document.getElementById('employeesModal');
            if (modal.classList.contains('show-modal')) {
                this.closeEmployeesModal();
            } else {
                this.openEmployeesModal('department');
            }
        });

        document.getElementById('addSubdepartmentEmployees')?.addEventListener('click', () => {
            const modal = document.getElementById('employeesModal');
            if (modal.classList.contains('show-modal')) {
                this.closeEmployeesModal();
            } else {
                this.openEmployeesModal('subdepartment');
            }
        });

        document.querySelector('.employees-modal .close-modal')?.addEventListener('click', () => {
            this.closeEmployeesModal();
        });

        document.getElementById('employeesModal')?.addEventListener('click', (e) => {
            if (e.target.id === 'employeesModal') {
                this.closeEmployeesModal();
            }
        });

        document.querySelector('.employee-search')?.addEventListener('input', 
            this.handleEmployeeSearch.bind(this));

        this.initializeDragAndDrop();
    }

    async openEmployeesModal(targetType) {
        const modal = document.getElementById('employeesModal');
        modal.classList.add('show-modal');
        
        try {
            const response = await fetch(`/dashboard/employees/available/${targetType}/${
                targetType === 'department' ? this.currentDepartmentId : this.currentSubdepartmentId
            }`);
            
            if (!response.ok) throw new Error('Ошибка получения списка сотрудников');
            
            const employees = await response.json();
            this.renderAvailableEmployees(employees, targetType);
        } catch (error) {
            console.error('Ошибка:', error);
        }
    }

    closeEmployeesModal() {
        const modal = document.getElementById('employeesModal');
        if (modal) {
            modal.classList.remove('show-modal');
        }
    }

    renderAvailableEmployees(employees, targetType) {
        const employeesList = document.querySelector('.employees-list');
        employeesList.innerHTML = '';

        employees.forEach(employee => {
            const employeeCard = document.createElement('div');
            employeeCard.className = 'employee-card';
            employeeCard.innerHTML = `
                <div class="employee-info">
                    <span class="employee-name">${employee.firstName} ${employee.lastName}</span>
                </div>
                <div class="employee-actions">
                    <button class="btn-assign" data-employee-id="${employee.userId}">
                        <i class="fas fa-plus"></i> Добавить
                    </button>
                </div>
            `;

            employeeCard.querySelector('.btn-assign').addEventListener('click', () => 
                this.assignEmployee(employee.userId, targetType));

            employeesList.appendChild(employeeCard);
        });
    }

    async assignEmployee(employeeId, targetType) {
        try {
            const response = await fetch(`/dashboard/employees/assign/${targetType}/${
                targetType === 'department' ? this.currentDepartmentId : this.currentSubdepartmentId
            }`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify([employeeId])
            });
    
            if (!response.ok) throw new Error('Ошибка при назначении сотрудника');
    
            await this.loadEmployeesList(targetType);
            
            if (window.departmentManager) {
                await window.departmentManager.loadUsers();
                await window.departmentManager.loadDepartments();
                
                if (window.departmentManager.searchableSelect) {
                    window.departmentManager.searchableSelect.reset();
                    window.departmentManager.setupSearchableSelect();
                }
                
                if (window.departmentManager.subDepartmentManager) {
                    await window.departmentManager.subDepartmentManager.loadUsers();
                    await window.departmentManager.subDepartmentManager.loadSubDepartments();
                    
                    if (window.departmentManager.subDepartmentManager.searchableSelect) {
                        window.departmentManager.subDepartmentManager.searchableSelect.reset();
                        window.departmentManager.subDepartmentManager.setupSearchableSelect();
                    }
                }
            }
    
            const availableResponse = await fetch(`/dashboard/employees/available/${targetType}/${
                targetType === 'department' ? this.currentDepartmentId : this.currentSubdepartmentId
            }`);
            
            if (!availableResponse.ok) throw new Error('Ошибка получения списка сотрудников');
            
            const employees = await availableResponse.json();
            this.renderAvailableEmployees(employees, targetType);
    
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Произошла ошибка при назначении сотрудника');
        }
    }

    async loadEmployeesList(targetType) {
        try {
            const response = await fetch(`/dashboard/${targetType}s/${
                targetType === 'department' ? this.currentDepartmentId : this.currentSubdepartmentId
            }/employees`);

            if (!response.ok) throw new Error('Ошибка получения списка сотрудников');

            const employees = await response.json();
            this.renderEmployeesList(employees, targetType);
        } catch (error) {
            console.error('Ошибка:', error);
        }
    }

    renderEmployeesList(employees, targetType) {
        const container = document.getElementById(`${targetType}EmployeesList`);
        container.innerHTML = '';

        employees.forEach(employee => {
            const employeeCard = document.createElement('div');
            employeeCard.className = 'employee-card draggable';
            employeeCard.draggable = true;
            employeeCard.dataset.employeeId = employee.userId;
            
            employeeCard.innerHTML = `
                <div class="employee-info">
                    <span class="employee-name">${employee.firstName} ${employee.lastName}</span>
                </div>
                <div class="employee-actions">
                    <button class="btn-remove" data-employee-id="${employee.userId}">
                        <i class="fas fa-times"></i> Удалить
                    </button>
                </div>
            `;

            employeeCard.querySelector('.btn-remove').addEventListener('click', () => 
                this.removeEmployee(employee.userId, targetType));

            container.appendChild(employeeCard);
        });
    }

    async removeEmployee(employeeId, targetType) {
        try {
            const response = await fetch(`/dashboard/employees/remove/${targetType}/${
                targetType === 'department' ? this.currentDepartmentId : this.currentSubdepartmentId
            }`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify([employeeId])
            });
    
            if (!response.ok) {
                const errorText = await response.text();
                if (errorText.includes("руководителя подотдела")) {
                    alert("Невозможно удалить руководителя подотдела. Сначала необходимо назначить нового руководителя или удалить подотдел.");
                    return;
                }
                throw new Error('Ошибка при удалении сотрудника');
            }
    
            await this.loadEmployeesList(targetType);
            
            if (window.departmentManager) {
                await window.departmentManager.loadUsers();
            }
            const subDepartmentManager = window.departmentManager?.subDepartmentManager;
            if (subDepartmentManager) {
                await subDepartmentManager.loadUsers();
            }
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Произошла ошибка при удалении сотрудника');
        }
    }

    handleEmployeeSearch(event) {
        const searchTerm = event.target.value.toLowerCase();
        const employeeCards = document.querySelectorAll('.employees-list .employee-card');

        employeeCards.forEach(card => {
            const employeeName = card.querySelector('.employee-name').textContent.toLowerCase();
            card.style.display = employeeName.includes(searchTerm) ? '' : 'none';
        });
    }

    initializeDragAndDrop() {
        const dragAreas = document.querySelectorAll('.employee-drag-area');

        dragAreas.forEach(area => {
            area.addEventListener('dragover', e => {
                e.preventDefault();
                area.classList.add('drag-over');
            });

            area.addEventListener('dragleave', () => {
                area.classList.remove('drag-over');
            });

            area.addEventListener('drop', e => {
                e.preventDefault();
                area.classList.remove('drag-over');
            });
        });
    }

    setCurrentDepartment(departmentId) {
        this.currentDepartmentId = departmentId;
        if (departmentId) {
            this.loadEmployeesList('department');
        }
    }

    setCurrentSubdepartment(subdepartmentId) {
        this.currentSubdepartmentId = subdepartmentId;
        if (subdepartmentId) {
            this.loadEmployeesList('subdepartment');
        }
    }
}

export default EmployeeManager; 