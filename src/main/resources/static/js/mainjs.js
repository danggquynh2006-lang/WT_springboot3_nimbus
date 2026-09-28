$(document).ready(function() {

	// ---------- Trang profile: hien thi thong tin nguoi dung dang nhap ----------
	if ($('#profile').length) {
		$.ajax({
			type: 'GET',
			url: '/users/me',
			dataType: 'json',
			contentType: 'application/json; charset=utf-8',
			beforeSend: function(xhr) {
				if (localStorage.token) {
					// Authorization: Bearer <token>
					xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.token);
				}
			},
			success: function(data) {
				$('#profile').text(data.fullName);
				$('#images').attr('src', data.images);
			},
			error: function() {
				alert('Sorry, you are not logged in.');
				window.location.href = '/login';
			}
		});
	}

	// ---------- Ham dang xuat ----------
	$('#logout').click(function() {
		localStorage.clear();
		window.location.href = '/login';
	});

	// ---------- Ham Login ----------
	$('#loginForm').on('submit', function(e) {
		e.preventDefault();
		var basicInfo = JSON.stringify({
			email: $('#email').val(),
			password: $('#password').val()
		});
		$.ajax({
			type: 'POST',
			url: '/auth/login',
			dataType: 'json',
			contentType: 'application/json; charset=utf-8',
			data: basicInfo,
			success: function(data) {
				localStorage.token = data.token; // luu JWT tren client
				window.location.href = '/user/profile';
			},
			error: function() {
				alert('Login Failed');
			}
		});
	});
});
