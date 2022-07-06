$(function(){
	
	/////////////////////////////////////////////////////////////////
	// ラジオボタン変更時
	/////////////////////////////////////////////////////////////////
	$('input:radio').on('change', function() {
		
		controllButton();
	});
	
	/////////////////////////////////////////////////////////////////
	// 作業開始ボタンクリック時
	/////////////////////////////////////////////////////////////////
	$('#btnStart').on('click', function() {
		
		register(true);
	});
	
	/////////////////////////////////////////////////////////////////
	// 作業終了ボタンクリック時
	/////////////////////////////////////////////////////////////////
	$('#btnStop').on('click', function() {
		
		register(false);
	});
	
	$('button[name="btnSend"]').on('click', function() {
	});
	
	/////////////////////////////////////////////////////////////////
	// 削除ボタンクリック時
	/////////////////////////////////////////////////////////////////
	$('#btnDelete').on('click', function() {
		
		deleteActual();
	});
	
	/////////////////////////////////////////////////////////////////
	// 送信ボタンクリック時
	/////////////////////////////////////////////////////////////////
	$('#btnSend').on('click', function() {
		
		send();
	});
	
	/////////////////////////////////////////////////////////////////
	// ボタン活性・非活性制御
	/////////////////////////////////////////////////////////////////
	function controllButton() {
		
		// ボタン非活性化
		disablButton();
		
		let issueRow = $('input:radio:checked').parents('tr');
		
		// 作業開始・終了ボタン
		if(issueRow.find('[name="isWorking"]').val() == $("#constIsWorking").val()) {
			
			$('#btnStop').prop('disabled', false);
			
		} else{
			
			$('#btnStart').prop('disabled', false);
		}
		
		// 削除ボタン
		if(issueRow.find('[name="thisActualHours"]').text() != $("#constHyphen").val()) {
			
			$('#btnDispModalDelete').prop('disabled', false);
			
		}
		
		// 送信ボタン
		if (canEnabledSendButton()) {
			
			$('#btnDispModalSend').prop('disabled', false);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// 送信ボタン活性可否
	/////////////////////////////////////////////////////////////////
	function canEnabledSendButton() {
		
		let issueRow = $('input:radio:checked').parents('tr');
		
		// 実績有無
		if (issueRow.find('[name="thisActualHours"]').text() == $("#constHyphen").val()) {
			
			return false;
		}
		
		// 作業中
		if (issueRow.find('[name="isWorking"]').val() == $("#constIsWorking").val()) {
			
			return false;
		}
		
		// 設定済み
		if (issueRow.find('[name="thisActualHours"]').text() == issueRow.find('[name="backlogActualHours"]').text()) {
			
			return false;
		}
		
		return true;
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業開始・終了登録
	//
	// @param isStart '1'：開始、'0'：終了
	/////////////////////////////////////////////////////////////////
	function register(isStart) {
		
		// ボタン非活性化
		disablButton();
		
		const requestParamRegister = {
				'issueId': $('input:radio:checked').val(),
				'isStart': isStart
		};
		
		$.ajax({
			url: '/register',
			type: 'POST',
			dataType : 'text',
			headers: {
				'X-XSRF-TOKEN': $.cookie('XSRF-TOKEN'),
				'Accept': 'application/json',
        		'Content-Type': 'application/json'
			},
			data: JSON.stringify(requestParamRegister)
		}).done(function(data) {
			registerSuccess(isStart, data);
		})
		.fail(function() {
			registerFail();
		})
		.always(function() {
			registerAlways();
		});
	}
	
	/////////////////////////////////////////////////////////////////
	// ボタン非活性化
	/////////////////////////////////////////////////////////////////
	function disablButton(){
		
		// クリック防止
		$('#btnStart').prop('disabled', true);
		$('#btnStop').prop('disabled', true);
		$('#btnDispModalDelete').prop('disabled', true);
		$('#btnDispModalSend').prop('disabled', true);
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業開始・終了登録成功時処理
	//
	// @param isStart true：開始、false：終了
	// @param actualHours
	/////////////////////////////////////////////////////////////////
	function registerSuccess(isStart, actualHours) {
		
		// 作業実績
		$('input:radio:checked').parents('tr').find('[name="thisActualHours"]').text(actualHours);
		
		// クリック防止解除
		if (isStart) {
			
			$('input:radio:checked').parents('tr').find('[name="workStatus"]').text($("#constWorkingText").val());
			
			$('input:radio:checked').parents('tr').find('[name="isWorking"]').val($("#constIsWorking").val());
			
		} else {
			
			$('input:radio:checked').parents('tr').find('[name="workStatus"]').text($("#constHyphen").val());
			
			$('input:radio:checked').parents('tr').find('[name="isWorking"]').val($("#constIsNotWorking").val());
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業開始・終了登録失敗時処理
	/////////////////////////////////////////////////////////////////
	function registerFail() {
		
		// 通知モーダル初期化
		initModalNotification();
		
		$('#modalNotification').find('i').addClass('bi bi-x-circle text-danger');
		$('#modalNotification').find('span').text($('#constMsgRefisterFail').val());
		
		// 通知モーダル表示
		$('#modalNotification').modal('show');
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業開始・終了登録終了時
	/////////////////////////////////////////////////////////////////
	function registerAlways() {
		
		// ボタン活性・非活性制御
		controllButton();
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業実績（記録結果）削除
	/////////////////////////////////////////////////////////////////
	function deleteActual() {
		
		// ボタン非活性化
		disablButton();
		
		const requestParamDelete = {
				'issueId': $('input:radio:checked').val()
		};
		
		$.ajax({
			url: '/delete',
			type: 'POST',
			dataType : 'text',
			headers: {
				'X-XSRF-TOKEN': $.cookie('XSRF-TOKEN'),
				'Accept': 'application/json',
        		'Content-Type': 'application/json'
			},
			data: JSON.stringify(requestParamDelete)
		})
		.done(function() {
			deleteSuccess();
		})
		.fail(function() {
			deleteFail();
		})
		.always(function() {
			deleteAlways();
		});
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業実績（記録結果）削除成功寺
	/////////////////////////////////////////////////////////////////
	function deleteSuccess() {
		
		// 作業実績
		$('input:radio:checked').parents('tr').find('[name="thisActualHours"]').text($("#constHyphen").val());
		
		$('input:radio:checked').parents('tr').find('[name="workStatus"]').text($("#constHyphen").val());
		
		$('input:radio:checked').parents('tr').find('[name="isWorking"]').val($("#constIsNotWorking").val());
		
		// 確認モーダル閉じる
		$('#modalDelete').modal('hide');
		
		// 通知モーダル初期化
		initModalNotification();
		
		$('#modalNotification').find('i').addClass('bi bi-check-circle text-success');
		$('#modalNotification').find('span').text($('#constMsgDeleteSuccess').val());
		
		// 通知モーダル表示
		$('#modalNotification').modal('show');
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業実績（記録結果）削除失敗時
	/////////////////////////////////////////////////////////////////
	function deleteFail() {
		
		// 確認モーダル閉じる
		$('#modalDelete').modal('hide');
		
		// 通知モーダル初期化
		initModalNotification();
		
		$('#modalNotification').find('i').addClass('bi bi-x-circle text-danger');
		$('#modalNotification').find('span').text($('#constMsgDeleteFail').val());
		
		$('#modalNotification').modal('show');
	}
	
	function deleteAlways() {
		
		// ボタン活性・非活性制御
		controllButton();
		
		$('#modalDelete').modal('hide')
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業実績送信
	/////////////////////////////////////////////////////////////////
	function send() {
		
		// ボタン非活性化
		disablButton();
		
		const requestParamSend = {
				'issueId': $('input:radio:checked').val(),
				'actualHours': $('input:radio:checked').parents('tr').find('[name="thisActualHours"]').text()
		};

		$.ajax({
			url: '/send',
			type: 'POST',
			dataType : 'text',
			headers: {
				'X-XSRF-TOKEN': $.cookie('XSRF-TOKEN'),
				'Accept': 'application/json',
        		'Content-Type': 'application/json'
			},
			data: JSON.stringify(requestParamSend)
		}).done(function() {
			sendSuccess();
		})
		.fail(function(msg) {
			sendFail(msg);
		})
		.always(function() {
			sendAlways();
		});
	}
	
	/////////////////////////////////////////////////////////////////
	// 作業実績送信成功寺
	/////////////////////////////////////////////////////////////////
	function sendSuccess() {
		
		let thisActualHours = $('input:radio:checked').parents('tr').find('[name="thisActualHours"]').text();
		
		$('input:radio:checked').parents('tr').find('[name="backlogActualHours"]').text(thisActualHours);
		
		// 確認モーダル閉じる
		$('#modalSend').modal('hide')
		
		// 通知モーダル初期化
		initModalNotification();
		
		$('#modalNotification').find('i').addClass('bi bi-check-circle text-success');
		$('#modalNotification').find('span').text($('#constMsgSendSuccess').val());
		
		// 通知モーダル表示
		$('#modalNotification').modal('show');
	} 
	
	/////////////////////////////////////////////////////////////////
	// 作業実績送信失敗時
	/////////////////////////////////////////////////////////////////
	function sendFail() {
		
		// 確認モーダル閉じる
		$('#modalSend').modal('hide')
		
		// 通知モーダル初期化
		initModalNotification();
		
		$('#modalNotification').find('i').addClass('bi bi-check-circle text-success');
		$('#modalNotification').find('span').text($('#constMsgSendFail').val());
		
		// 通知モーダル表示
		$('#modalNotification').modal('show');
	} 
	
	/////////////////////////////////////////////////////////////////
	// 作業実績送信終了後
	/////////////////////////////////////////////////////////////////
	function sendAlways() {
		
		// ボタン活性・非活性制御
		controllButton();
		
		$('#modalSend').modal('hide')
	}
	
	/////////////////////////////////////////////////////////////////
	// 通知モーダル初期化
	/////////////////////////////////////////////////////////////////
	function initModalNotification() {
		
		$('#modalNotification').find('i').removeClass();
		$('#modalNotification').find('span').text("");
	} 
});