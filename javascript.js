document.addEventListener('DOMContentLoaded', function () {
    // 1. Capturamos el botón y los campos de filtro por su ID
    const btnFiltrar = document.getElementById('btnFiltrar');
    const filtroPrioridad = document.getElementById('filtroPrioridad');
    const filtroEstado = document.getElementById('filtroEstado');
    
    // 2. Escuchamos el evento click en el botón "Aplicar Filtro"
    btnFiltrar.addEventListener('click', function () {
        const prioridadSeleccionada = filtroPrioridad.value.toLowerCase();
        const estadoSeleccionado = filtroEstado.value.toLowerCase();

        // 3. Obtenemos todas las filas de la tabla de tickets
        const filas = document.querySelectorAll('#tablaTickets .fila-ticket');

        filas.forEach(fila => {
            // Obtenemos el texto de la prioridad y del estado de cada fila
            const textoPrioridad = fila.querySelector('.celda-prioridad').textContent.toLowerCase();
            const textoEstado = fila.querySelector('.celda-estado').textContent.toLowerCase();

            // Verificamos si la fila cumple con los filtros seleccionados
            const coincidePrioridad = (prioridadSeleccionada === 'todos') || textoPrioridad.includes(prioridadSeleccionada);
            const coincideEstado = (estadoSeleccionado === 'todos') || textoEstado.includes(estadoSeleccionado);

            // 4. Si coincide con ambos filtros se muestra, si no se oculta
            if (coincidePrioridad && coincideEstado) {
                fila.style.display = ''; // Muestra la fila
            } else {
                fila.style.display = 'none'; // Oculta la fila
            }
        });
    });
});