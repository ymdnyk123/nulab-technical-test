$(function(){
	
	/**
	 * 実績自動設定切り替え
	 */
	$('input[type=checkbox]').on('change', function() {
		
		var isAutoSetting = '0';
		
		if ($(this).prop('checked')) {
			
			isAutoSetting = '1';
		}
		
		var issueId = $(this).parents('tr').find('[name="issueId"]').val();
		
		$.ajax({
			url: '/backlogApp/issues/switchAutoSetting',
			type: 'GET',
			dataType : 'json',
			data: {
				'issueId': issueId,
				'isAutoSetting': isAutoSetting
			}
		})
	});
	
	$('button[name="btnRecording"]').on('click', function() {
		
		var isRecording = '0';
		
		if ($(this).children('span[name="spinner"]').hasClass('spinner-border')) {
			
			// 記録停止
			$(this).children('span[name="spinner"]').removeClass('spinner-border');
			
			$(this).children('span[name="recordingText"]').text($('#recordingStatusStop').val());
			
		} else {
			
			// 記録開始
			isRecording = '1';
			
			$(this).children('span[name="spinner"]').addClass('spinner-border');
			
			$(this).children('span[name="recordingText"]').text($('#recordingStatusRecording').val());
		}
		
		var issueId = $(this).parents('tr').find('[name="issueId"]').val();
		
		$.ajax({
			url: '/backlogApp/issues/switchRecording',
			type: 'GET',
			dataType : 'json',
			data: {
				'issueId': issueId,
				'isRecording': isRecording
			}
		})
	});
});