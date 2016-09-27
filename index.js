/**
 * Created by ltjin on 16/9/26.
 */
import {Alert, Platform, Linking, NativeModules} from 'react-native'

export default class AppUpdate {

    static init(params) {
        fetch(params.url,
            {
                method: "post",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(params.body)
            }).then(res => {
            res.json().then(body => {
                if(body.flag === '00'){
                    let str = '当前版本:' + params.body.version + ',最新版本:' +body.lastestVer;
                    if(body.updateContent && body.updateContent !== ''){
                        str += ',本次更新内容:\n' + body.updateContent.replace(/#/g, '\n');
                    }
                    Alert.alert(
                        '发现新版本',
                        str,
                        [
                            {text: '取消'},
                            {
                                text: '确认', onPress: () => {
                                this._update(body.updateUrl);
                            }
                            }
                        ]
                    )
                }
            }).catch(error=> {
                console.log(error);
            });
        }).catch(err=> {
            console.log(err);
        });
    }

    static _update(url){
        if(Platform.OS === 'android'){
            // NativeModules.AppUpdate.update(url);
        }else if(Platform.OS === 'ios'){
            Linking.openURL(url).catch(err=>{
                Alert.alert(
                    '版本更新',
                    '版本更新失败',
                    [
                        {text: '确认'}
                    ]
                )
            });
        }
    }
}