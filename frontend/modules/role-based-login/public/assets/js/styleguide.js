$(function() {

  /* Style Guide Stuffs: Menu */
  var offset = $("#styleguide-menu").offset().top;

  $(window).scroll(function () {
    if ($(window).scrollTop() > offset) {
       $('#page').addClass('menu-fixed');
     }
     if ($(window).scrollTop() < offset) {
       $('#page').removeClass('menu-fixed');
     }
  });

  $("#styleguide-menu a").click(function (e) {
    e.preventDefault();
    $("#styleguide-menu a").removeClass("active");
    $(this).addClass("active");
    var clickedItem = $(this).attr("href");
    var target = $("section"+clickedItem).offset().top;

    $("body, html").stop().animate({
      scrollTop: target,
    }, 500);

  });

  /* Charts */
  Morris.Donut({
    element: 'icondonut',
    colors: ["#d71b36", "#a3a7a9", "#797d82", "#484c55"],
    data: [
      {label: "Item 1", value: 12},
      {label: "Item 2", value: 30},
      {label: "Item 3", value: 20},
      {label: "Item 4", value: 20}
    ]
  });

  Morris.Donut({
    element: 'icondonut2',
    colors: ["#ff4e00", "#33647f", "#415464", "#768693"],
    data: [
      {label: "Item 1", value: 12},
      {label: "Item 2", value: 30},
      {label: "Item 3", value: 20},
      {label: "Item 4", value: 20}
    ]
  });

  Morris.Line({
    element: 'iconline',
    lineColors: ["#d71b36", "#a3a7a9", "#797d82", "#484c55"],
    data: [
      { y: '2006', a: 100, b: 90 },
      { y: '2007', a: 75,  b: 65 },
      { y: '2008', a: 50,  b: 40 },
      { y: '2009', a: 75,  b: 65 },
      { y: '2010', a: 50,  b: 40 },
      { y: '2011', a: 75,  b: 65 },
      { y: '2012', a: 100, b: 90 }
    ],
    xkey: 'y',
    ykeys: ['a', 'b'],
    labels: ['Series A', 'Series B']
  });

  Morris.Area({
    element: 'iconarea',
    lineColors: ["#d71b36", "#a3a7a9"],
    data: [
      { y: '2006', a: 100, b: 90 },
      { y: '2007', a: 75,  b: 65 },
      { y: '2008', a: 50,  b: 40 },
      { y: '2009', a: 75,  b: 65 },
      { y: '2010', a: 50,  b: 40 },
      { y: '2011', a: 75,  b: 65 },
      { y: '2012', a: 100, b: 90 }
    ],
    xkey: 'y',
    ykeys: ['a', 'b'],
    labels: ['Series A', 'Series B']
  });

  /* Tree Style Navigation */
  $('.tree-toggle').click(function () {
    $(this).parent().children('ul.tree').toggle(200);
    $(this).toggleClass('collapsed');
  });

  $('.tree-toggle').keypress(function(e){
    if(e.which == 13) {
      $(this).click();
    }
  });

  /* Main Navigation Bar */
  $('.burger').click(function (e) {
    e.preventDefault();
    $(this).parent().children('div.mobile-menu').fadeToggle( "linear" );
    $(this).toggleClass("active");
    /* to fix style guide */
    //$('body,html').toggleClass('noscroll');
  });

  $('.has-submenu').click(function (e) {
    e.preventDefault();
    $(this).find('ul').toggleClass('visible');
  });

  /* Accordions */
  $('.minimalist-accordion .card-header').click(function (e) {
    $(this).parent().toggleClass('active');
    $(this).parent().siblings().removeClass('active');
  });

  /* Tooltips */
  $(function () {
    $('[data-bs-toggle="tooltip"]').tooltip();
  });

  /* Carousel - Slick Slider */
  $(document).ready(function(){
    $('.slick-slider').slick({
      accessibility: true,
      arrow: true,
      dots: true,
      slidesToShow: 1,
      slidesToScroll: 1,
    });
  });

  /* Combination Dropdown */
  $('#select-beast').selectize({
    create: true,
    sortField: {
      field: 'text',
      direction: 'asc'
    },
    dropdownParent: 'body'
  });

  /* Calendar Date Picker */
  $(function() {
    $('input[name="daterange"]').daterangepicker({
      showDropdowns: true
    });

    $('input[name="singledate"]').daterangepicker({
      singleDatePicker: true,
      showDropdowns: true
    });
  });

  /* Drag & Drop Control */
  $("#list1, #list2").dragsort({
    dragSelector: "div",
    dragBetween: true,
    dragEnd: saveOrder,
    placeHolderTemplate: "<li class='placeHolder'><div></div></li>"
  });

  function saveOrder() {
    var data = $("#list1 li").map(function() { return $(this).children().html(); }).get();
    $("input[name=list1SortOrder]").val(data.join("|"));
  };

  /* Number Slider Control */
  $('#ex1').slider({
    formatter: function(value) {
      return 'Current value: ' + value;
    }
  });

  $("#ex2").slider({});

  /* Style Guide Stuffs: Copy to clipboard button */
  if (typeof $.uf === 'undefined') {
    $.uf = {};
  }

  $.uf.copy = function (button) {

    var clipboard = new Clipboard(button, {
      target: function(trigger) {
        return trigger.nextElementSibling;
      }
    });

    clipboard.on('success', function(e) {
      setTooltip(e.trigger, 'Copied!');
      hideTooltip(e.trigger);
    });

    clipboard.on('error', function(e) {
      setTooltip(e.trigger, 'Failed!');
      hideTooltip(e.trigger);
    });

    function setTooltip(btn, message) {
      $(btn)
        .attr('data-original-title', message)
        .tooltip('show');
    }

    function hideTooltip(btn) {
      setTimeout(function() {
        $(btn).tooltip('hide')
          .attr('data-original-title', "");
      }, 1000);
    }

    // Tooltip
    $(button).tooltip();
  };

    // Link all copy buttons
  $.uf.copy('.copy-btn');

  /* Toggle Alternative Version */
  $('.toggle-buttons button').click(function () {
    $(this).prev('label').toggleClass('active');
    $(this).next('label').toggleClass('active');
  });

  /* Three States Toggle Button */
  $('input[type="range"].range-toggle').change(function () {
    var range = $(this).val();

    switch(range) {
      case "1":
        $(this).removeClass("range-neutral range-true");
        $(this).addClass("range-false");
        break;
      case "2":
        $(this).removeClass("range-false range-true");
        $(this).addClass("range-neutral");
        break;
      case "3":
        $(this).removeClass("range-neutral range-false");
        $(this).addClass("range-true");
        break;
    }
  });

  //Password hide/show
  $('input[type="password"]').password({
    eyeClass: 'fa',
    eyeOpenClass: 'fa-eye',
    eyeCloseClass: 'fa-eye-slash'
  });

  /* Child Row Tables  */
  var table = $('#example').DataTable({
    "ajax": "./development/ajax/data/objects.json",
    "bSort": false,
    "searching": false,
    "bInfo": false,
    "bLengthChange": false,
   
    "language": {
      "paginate": {
        "next": "»",
        "previous": "«"
      }
    },
    "columns": [
      {
        "className":      'details-control',
        "data":           null,
        "defaultContent": ''
      },
      { "data": "name" },
      { "data": "position" },
      { "data": "office" },
      { "data": "salary" }
    ],
    "dom": 'rtp'
  });


   

  /* Formatting function for row details - modify as you need */
  function format(d) {
    // `d` is the original data object for the row
    return '<table cellpadding="5" cellspacing="0" border="0" class="child-row-table">'+
      '<tr>'+
      '<td>Full name:</td>'+
      '<td>'+d.name+'</td>'+
      '</tr>'+
      '<tr>'+
      '<td>Extension number:</td>'+
      '<td>'+d.extn+'</td>'+
      '</tr>'+
      '<tr>'+
      '<td>Extra info:</td>'+
      '<td>And any further details here (images etc)...</td>'+
      '</tr>'+
      '</table>';
  }

  /* Add event listener for opening and closing details */
  $('#example tbody').on('click', 'td.details-control', function() {
    var tr = $(this).closest('tr');
    var row = table.row( tr );

    if ( row.child.isShown() ) {
      // This row is already open - close it
      row.child.hide();
      tr.removeClass('shown');
    }
    else {
      // Open this row
      row.child( format(row.data()) ).show();
      tr.addClass('shown');
    }
  } );

  /* Sortable Column Tables */
  $('#example2').DataTable({
    "ajax": "./development/ajax/data/objects.json",
    "bSort": true,
    "searching": false,
    "bInfo": false,
    "bLengthChange": false,
    "language": {
      "paginate": {
        "next": "»",
        "previous": "«"
      }
    },
    "columns": [
      { "data": "name" },
      { "data": "position" },
      { "data": "office" },
      { "data": "salary" }
    ],
    "dom": 'rtp'
  });

  /* Filterable Column Tables */
  // Setup - add a text input to each header cell
  $('#example3 thead tr:eq(0) th').each( function() {
    var title = $('#example3 thead tr:eq(0) th').eq( $(this).index()).text();

    $(this).html( '<i class="fa fa-filter" aria-hidden="true"></i><span>'+title+'</span><input class="form-control input-sm filter-search" type="text" placeholder="Search'+title+'">');
  });

  var table3 = $('#example3').DataTable({
    "ajax": "./development/ajax/data/objects.json",
    "bSort": false,
    "orderCellsTop": true,
    "bInfo": false,
    "bLengthChange": false,
    "language": {
      "paginate": {
        "next": "»",
        "previous": "«"
      }
    },
    "columns": [
      { "data": "name" },
      { "data": "position" },
      { "data": "office" },
      { "data": "salary" }
    ],
    "dom": 'frtp'
  });

  // Apply the search
  table3.columns().every(function (index) {
    $('#example3 thead tr:eq(0) th:eq(' + index + ') input').on('keyup change', function () {
      table3.column($(this).parent().index() + ':visible')
        .search(this.value)
        .draw();

      if($(this).val()!="") {
        $('#example3 thead tr:eq(0) th:eq(' + index + ') i.fa.fa-filter').addClass("applied");
        $(this).parent().find('span.clear-btn').css("display", "block");
      } else {
        $('#example3 thead tr:eq(0) th:eq(' + index + ') i.fa.fa-filter').removeClass("applied");
        $(this).parent().find('span.clear-btn').css("display", "none");
      }
    });
  });

  // Filter icon functionality
  $("i.fa.fa-filter").on("click", function(e){
    var th = $(this).parent();

    if($(this).hasClass('selected')){
      th.find('span').css("display", "block");
      th.find('input').css("display", "none");
      th.find('span.clear-btn').css("display", "none");
    } else {
      th.find('span').css("display", "none");
      th.find('input').css("display", "block");

      if(th.find('input').val()!=""){
        th.find('span.clear-btn').css("display", "block");
      }
    }

    $(this).toggleClass('selected');
    e.stopPropagation();
  });

  function tog(v){
    return v ? 'addClass' : 'removeClass';
  }

  $(document).on('input', '.filter-search', function(){
    $(this)[tog(this.value)]('x');
  }).on('mousemove', '.x', function( e ){
    $(this)[tog(this.offsetWidth-12 < e.clientX-this.getBoundingClientRect().left)]('onX');
  }).on('touchstart click', '.onX', function( ev ){
    ev.preventDefault();
    $(this).removeClass('x onX').val('').change();
  });
});
