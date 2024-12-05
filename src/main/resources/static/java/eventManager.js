class EventManager {
    constructor() {
        this.eventForm = document.getElementById('createEventForm');
        this.meetingForm = document.getElementById('createMeetingForm');
        this.recipientsModal = document.getElementById('recipientsModal');
        this.selectedRecipients = new Set();
        this.currentFormType = null;
        this.recipients = [];
        
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        if (this.eventForm) {
            this.eventForm.addEventListener('submit', (e) => this.handleEventSubmit(e));
        }
        if (this.meetingForm) {
            this.meetingForm.addEventListener('submit', (e) => this.handleMeetingSubmit(e));
        }

        document.querySelectorAll('.btn-select-recipients').forEach(button => {
            button.addEventListener('click', (e) => {
                this.currentFormType = e.target.dataset.formType;
                this.openRecipientsModal();
            });
        });

        const closeButton = document.querySelector('.close');
        if (closeButton) {
            closeButton.addEventListener('click', () => this.closeRecipientsModal());
        }

        const searchInput = document.getElementById('recipientSearch');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
        }
    }

    async loadAvailableRecipients() {
        try {
            console.log('Loading available recipients');
            const response = await fetch('/event/available-recipients');
            if (!response.ok) throw new Error('Ошибка загрузки получателей');
            
            this.recipients = await response.json();
            this.renderRecipientsList(this.recipients);
        } catch (error) {
            console.error('Error loading recipients:', error);
        }
    }

    renderRecipientsList(recipients) {
        const recipientsList = document.querySelector('.recipients-list');
        if (!recipientsList) return;
    
        const html = [];
    
        if (recipients.departments?.length > 0) {
            recipients.departments.forEach(dept => {
                html.push(`
                    <div class="recipient-item" data-id="${dept.id}" data-type="department">
                        <span class="recipient-name">${dept.name}</span>
                        <button class="btn-add-recipient">Добавить</button>
                    </div>
                `);
            });
        }
    
        if (recipients.subdepartments?.length > 0) {
            recipients.subdepartments.forEach(subdept => {
                html.push(`
                    <div class="recipient-item" data-id="${subdept.id}" data-type="subdepartment">
                        <span class="recipient-name">${subdept.name}</span>
                        <button class="btn-add-recipient">Добавить</button>
                    </div>
                `);
            });
        }
    
        recipientsList.innerHTML = html.join('');
    
        document.querySelectorAll('.btn-add-recipient').forEach(button => {
            button.addEventListener('click', (e) => {
                const item = e.target.closest('.recipient-item');
                const name = item.querySelector('.recipient-name').textContent;
                this.addRecipient(item.dataset.type, item.dataset.id, name);
                button.disabled = true;
                button.textContent = 'Добавлено';
            });
        });
    }

    handleSearch(query) {
        const searchQuery = query.toLowerCase();
        const filteredRecipients = {
            departments: this.recipients.departments?.filter(dept => 
                dept.name.toLowerCase().includes(searchQuery)
            ) || [],
            subdepartments: this.recipients.subdepartments?.filter(subdept => 
                subdept.name.toLowerCase().includes(searchQuery)
            ) || []
        };
        this.renderRecipientsList(filteredRecipients);
    }

    addRecipient(type, id, name) {
        const key = `${type}-${id}`;
        if (!this.selectedRecipients.has(key)) {
            this.selectedRecipients.add(key);
            this.updateSelectedRecipientsInForm(this.currentFormType);
        }
    }

    updateSelectedRecipientsInForm(formType) {
        const container = document.querySelector(`#${formType}Form .selected-recipients-container`);
        if (!container) return;
    
        container.innerHTML = Array.from(this.selectedRecipients).map(key => {
            const [type, id] = key.split('-');
            const recipient = type === 'department' 
                ? this.recipients.departments.find(dept => dept.id === parseInt(id))
                : this.recipients.subdepartments.find(subdept => subdept.id === parseInt(id));
    
            if (!recipient) return '';
            
            return `
                <div class="selected-recipient" data-key="${key}">
                    <span>${recipient.name}</span>
                    <span class="remove-recipient">&times;</span>
                </div>
            `;
        }).join('');
    
        container.querySelectorAll('.remove-recipient').forEach(removeBtn => {
            removeBtn.addEventListener('click', (e) => {
                const recipientElement = e.target.closest('.selected-recipient');
                this.selectedRecipients.delete(recipientElement.dataset.key);
                this.updateSelectedRecipientsInForm(formType);
            });
        });
    }

    openRecipientsModal() {
        this.loadAvailableRecipients();
        this.recipientsModal.classList.add('show-modal');
    }

    closeRecipientsModal() {
        this.recipientsModal.classList.remove('show-modal');
    }

    async handleEventSubmit(e) {
        e.preventDefault();
        const formData = new FormData(this.eventForm);
        
        const [type, id] = Array.from(this.selectedRecipients)[0].split('-');
        
        const eventData = {
            title: formData.get('eventTitle'),
            description: formData.get('eventDescription'),
            startTime: formData.get('eventStartTime'),
            endTime: formData.get('eventEndTime'),
            location: formData.get('eventLocation')
        };
    
        try {
            const response = await fetch(`/event/createEvent/${type}/${id}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(eventData)
            });
    
            if (!response.ok) throw new Error('Ошибка создания события');
            
            this.eventForm.reset();
            this.selectedRecipients.clear();
            this.updateSelectedRecipientsInForm('event');
            alert('Событие успешно создано!');
        } catch (error) {
            console.error('Error creating event:', error);
            alert('Ошибка при создании события');
        }
    }

    async handleMeetingSubmit(e) {
        e.preventDefault();
        const formData = new FormData(this.meetingForm);
        
        const [type, id] = Array.from(this.selectedRecipients)[0].split('-');

        const meetingData = {
            topic: formData.get('meetingTopic'),
            agenda: formData.get('meetingAgenda'),
            startTime: formData.get('meetingStartTime'),
            endTime: formData.get('meetingEndTime'),
        };
    
        try {
            const response = await fetch(`/event/createMeeting/${type}/${id}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(meetingData)
            });
    
            if (!response.ok) throw new Error('Ошибка создания мероприятия');
            
            this.meetingForm.reset();
            this.selectedRecipients.clear();
            this.updateSelectedRecipientsInForm('meeting');
            alert('Мероприятие успешно создано!');
        } catch (error) {
            console.error('Error creating meeting:', error);
            alert('Ошибка при создании мероприятия');
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const eventManager = new EventManager();
});