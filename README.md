# cordova-plugin-pictureselector

一个基于[PictureSelector](https://github.com/LuckSiege/PictureSelector)库开发的图片选择器，仅使用了库中的图片全屏预览、图片压缩、图片多选、权限说明、快速滑动选择等功能，其他功能自行研究修改。

## 功能

- 相册目录
- 多选图
- 全屏预览选中的图片
- 图片压缩
- 权限说明弹窗
- 权限拒绝回调弹窗
- 快速滑动选择

## 安装要求

仅支持Android

插件使用的是 PictureSelector库 v3.11.2 最新版本，开发时只有使用cordova-android 12.0.1 打包才不会报错，其他版本具体打包会不会报错未测试，请自行测试。

## 安装

`cordova plugin add https://github.com/Wgga/cordova-plugin-pictureselector.git`

## 使用演示

自行去PictureSelector库[app/demo](https://github.com/LuckSiege/PictureSelector/tree/version_component/app/demo)路径内下载APK查看

## 使用方式

```javascript
let options = {
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
// 以上参数为默认参数，options中不传递则使用这些默认参数
pictureselector.getPictures((success: any) => {
    console.log("getPictures success:", JSON.stringify(success));
}, (error: any) => {
    console.log("getPictures error:", JSON.stringify(error));
}, options);
```

ionic 中使用本插件，需要声明： `declare let pictureselector:any`