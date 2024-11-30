const calendar = document.getElementById("calendar");

const monthYear = document.getElementById("month-year");

const daysContainer = document.getElementById("days");

const prevButton = document.getElementById("prev");

const nextButton = document.getElementById("next");

let currentDate = new Date();

// Пример задач

const tasks = {

  "2024-12-01": ["Собрание команды", "Дедлайн проекта"],

  "2024-12-05": ["Отчет по кварталу"],

  "2024-12-10": ["Демо продукта"],

};

function renderCalendar(date) {

  const year = date.getFullYear();

  const month = date.getMonth();

  const firstDayIndex = new Date(year, month, 1).getDay() || 7;

  const lastDay = new Date(year, month + 1, 0).getDate();

  monthYear.textContent = `${date.toLocaleString("ru", { month: "long" })} ${year}`;

  daysContainer.innerHTML = "";

  // Пустые дни перед началом месяца

  for (let i = 1; i < firstDayIndex; i++) {

    const emptyDay = document.createElement("div");

    daysContainer.appendChild(emptyDay);

  }

  // Дни месяца

  for (let day = 1; day <= lastDay; day++) {

    const dayElement = document.createElement("div");

    dayElement.classList.add("day");

    const dayNumber = document.createElement("div");

    dayNumber.classList.add("day-number");

    dayNumber.textContent = day;

    dayElement.appendChild(dayNumber);

    const tasksContainer = document.createElement("div");

    tasksContainer.classList.add("tasks");

    // Форматируем дату как ключ

    const dateKey = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;

    if (tasks[dateKey]) {

      tasks[dateKey].forEach(task => {

        const taskElement = document.createElement("div");

        taskElement.textContent = `• ${task}`;

        tasksContainer.appendChild(taskElement);

      });

    }

    dayElement.appendChild(tasksContainer);

    const today = new Date();

    if (day === today.getDate() && month === today.getMonth() && year === today.getFullYear()) {

      dayElement.classList.add("today");

    }

    daysContainer.appendChild(dayElement);

  }

}

prevButton.addEventListener("click", () => {

  currentDate.setMonth(currentDate.getMonth() - 1);

  renderCalendar(currentDate);

});

nextButton.addEventListener("click", () => {

  currentDate.setMonth(currentDate.getMonth() + 1);

  renderCalendar(currentDate);

});

renderCalendar(currentDate);