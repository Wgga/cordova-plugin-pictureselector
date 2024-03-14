var exec = require('cordova/exec');

if (typeof Object.assign != 'function') {
	Object.assign = function (target) {
		'use strict';
		if (target == null) {
			throw new TypeError('Cannot convert undefined or null to object');
		}

		target = Object(target);
		for (var index = 1; index < arguments.length; index++) {
			var source = arguments[index];
			if (source != null) {
				for (var key in source) {
					if (Object.prototype.hasOwnProperty.call(source, key)) {
						target[key] = source[key];
					}
				}
			}
		}

		return target;
	};
}

var defaults = {
	selectionMode: 2,         // 选择模式,（单选:1 多选:2）
	maxSelectNum: 9,          // 最大选择图片数量
	isCompress: false,        // 是否压缩图片
	isPermission: false,      // 是否开启权限说明弹窗
	isSlideSelect: false,     // 是否滑动选择
	isShowCamera: true,       // 是否显示相机
	isShowPreview: true,      // 是否显示预览
	isPreviewFull: true,      // 是否开启预览点击全屏效果
	isOpenSound: false,       // 是否开启点击声音
	isMaxSelectMask: false,   // 是否开启最大选择数量遮罩
};

var transformResult = function (result) {
	if (result && result.images) {
		var i, img;
		for (i = 0; i < result.images.length; i++) {
			img = result.images[i];
			if (img && img.path) {
				img.uri = 'file://' + img.path;
			}
		}
	}
};

module.exports = {
	/*
	 * 获取图片地址
	 * @param onSuccess 成功回调
	 * @param onFail 失败回调
	 * @param params 参数
	 */
	getPictures: function (onSuccess, onFail, params) {
		var options = Object.assign({}, defaults, params);

		var success = function (result) {
			if (typeof onSuccess == 'function') {
				transformResult(result);
				onSuccess.apply(null, arguments);
			}
		};

		exec(success, onFail, 'pictureselector', 'getPictures', [options]);
	},

	/*
	 * 获取图片Blob
	 * @param onSuccess 成功回调
	 * @param onFail 失败回调
	 * @param params 参数
	 */
	getPicBlob: function (onSuccess, onFail, params) {
		exec(onSuccess, onFail, 'pictureselector', 'getPicBlob', [params]);
	}
};