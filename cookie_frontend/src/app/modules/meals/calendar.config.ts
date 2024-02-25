import { CalendarOptions } from '@fullcalendar/core';
import timeGridPlugin from '@fullcalendar/timegrid';
import Tooltip from 'tooltip.js';

export const calendarConfig: CalendarOptions = {
  initialView: 'timeGridWeek',
  plugins: [timeGridPlugin],
  headerToolbar: {
    left: 'title',
    right: 'prev,next',
  },
  allDaySlot: false,
  slotMinTime: '06:00:00',
  slotMaxTime: '23:00:00',
  slotLabelFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  },
  slotDuration: '00:30:00',
  expandRows: true,
  height: 600,
  dayHeaderFormat: {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    omitCommas: false,
  },
  firstDay: 1,
  nowIndicator: true,
  eventTimeFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  },
  eventDidMount: function (info) {
    var tooltip = new Tooltip(info.el, {
      title: info.event.extendedProps['description'],
      placement: 'top',
      trigger: 'hover',
      container: 'body',
    });
  },
};
