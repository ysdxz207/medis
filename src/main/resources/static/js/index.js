var index = {
    base: $('#base').val(),
    $selectDB: $('#select_db'),
    $select:  $("#select_key"),
    $inputHost: $('#input_host'),
    $inputPort: $('#input_port'),
    $inputPass: $('#input_pass'),
    format: false
};
(function ($, index) {

    /**
     * 载入标签列表
     */
    index.loadTags = function () {
        $.get(index.base + "/tag/tags", function (data) {

            index.$select.typeahead({
                source: data,
                showHintOnFocus: true,
                displayText: function (item) {
                    return item.name + "&nbsp;&nbsp;" + item.value;
                },
                afterSelect: function (item) {
                    index.$select.val(item.value);
                    index.keys(item.value);
                }

            });
        }, 'json');

    };

    /**
     * 获取值
     * @param key
     */
    index.getValue = function(key) {
        var msg = index.validateNoPass()
        if (msg) {
            salert(msg);
            return;
        }
        $.get(index.base + '/redis/get', {db: index.$selectDB.val(), key: key}, function (data) {
            $('#input_key').val(key);
            $('#text_value').val(data);
        })
    };

    /**
     * 查询key
     * @param key
     */
    index.keys = function(key) {
        var msg = index.validateNoPass()
        if (msg) {
            salert(msg);
            return;
        }

        var $list = $('#list_key');
        $.get(index.base + "/redis/keys", {
            db: index.$selectDB.val(),
            key: key
        }, function (data) {
            $list.empty();
            $.each(data, function (i, value) {
                var $tr = $('<li style="cursor: pointer">' + value + '</li>').on('click', function () {
                    index.getValue(value);
                });
                $list.append($tr);
            })
        }, 'json');
    };





    /**
     * 删除
     * @param key
     */
    index.deleteRedis = function(key) {

        $.get(index.base + '/redis/delete', {db: index.$selectDB.val(), key: key}, function (data) {
            if (data > 0) {
                salert('删除成功');
            } else {
                salert('删除失败');
            }
        })
    };


    /**
     * 修改或添加
     */
    index.editRedis = function() {

        var key = $('#input_key').val();
        var value = $('#text_value').val();
        $.get(index.base + '/redis/edit', {db: index.$selectDB.val(), key: key, value: value}, function (data) {
            if (data) {
                salert('修改成功');
            } else {
                salert('修改失败');
            }
        })
    };


    index.bind = function () {
        $('#btn_submit').on('click', function () {
            index.keys($('#select_key').val())
        });

        $('#select_opration').on('change', function () {
            switch (this.value) {
                case 'delete':
                    salert('确定删除？',function(choose){
                        if (choose) {
                            index.deleteRedis($('#input_key').val())
                        }
                    });
                    break;
                case 'edit':
                    index.editRedis();
                    break;
                case 'format':
                    index.toggleFormat(this);
                    break;
            }

            $(this).val('');
        });

        $('#btn_save_conf').on('click', function () {
            index.saveConfAndConnect();
        });
        $('#btn_delete_conf').on('click', function () {
            index.deleteConf();
        });
        $('#select_conf').on('change', function () {
            var confStr = $(this).val();
            if (confStr == '') {
                index.$inputHost.val('');
                index.$inputPort.val('');
                index.$inputPass.val('');
                index.$selectDB.empty();
                return;
            }
            var confArr = confStr.split(':');
            var host = confArr[0];
            var port = confArr[1];
            var pass = confArr[2];
            index.$inputHost.val(host);
            index.$inputPort.val(port);
            index.$inputPass.val(pass);
            index.saveConfAndConnect();
        });
    };

    /**
     * 保存配置并连接redis
     */
    index.saveConfAndConnect = function(){

        var msg = index.validateNoPass()
        if (msg) {
            salert(msg);
            return;
        }

        var host = index.$inputHost.val();
        var port = index.$inputPort.val();
        var pass = index.$inputPass.val();

        $.ajax({
            type: 'get',
            async: false,
            url: index.base + "/redis/connect",
            data: {
                host: host,
                port: port,
                pass: pass
            },
            success: function (data) {
                if (!data) {
                    salert("配置不正确！");
                } else {
                    //统计
                    index.count();
                }

            }
        });

    };

    /**
     * 删除配置
     */
    index.deleteConf = function () {
        var host = index.$inputHost.val();
        var port = index.$inputPort.val();
        var pass = index.$inputPass.val();

        $.get(index.base + "/redis/conf/delete", {
            host: host,
            port: port,
            pass: pass
        }, function (data) {
            if (data) {
                salert("删除成功！");
            } else {
                salert("删除失败！");
            }
        }, 'json');
    };


    /**
     * 获取数据库统计
     */
    index.count = function () {

        $.get(index.base + "/redis/count", function (data) {
            if (data) {
                index.$selectDB.empty();
                $.each(data, function (i, count) {
                    var $option = $('<option value="' + count.name.replace(/[^0-9]/ig,"") + '">'
                        + count.name + '&nbsp;&nbsp;&nbsp;&nbsp;key数量:'
                        + count.count + '</option>');
                    index.$selectDB.append($option);
                });
            }
        }, 'json');
    };

    index.toggleFormat = function(btn){
        if (!index.format) {
            var strJson = $('#text_value').val();
            if (!strJson) {
                return;
            }
            var obj = JSON.parse(strJson);
            var str = JSON.stringify(obj, undefined, 2);
            $('#text_value').val(str);
            index.format = true;
            btn.value = "恢复格式";
        } else {
            index.getValue($('#input_key').val());
            index.format = false;
            btn.value = "格式化JSON";
        }

    };

    /**
     * 校验参数
     * @returns {*}
     */
    index.validateNoPass = function() {
        var host = index.$inputHost.val();
        var port = index.$inputPort.val();
        var pass = index.$inputPass.val();

        if (!host) {
            return "未填入host";
        }
        if (!port) {
            return "未填入port";
        }
        if (!pass) {
            return "未填入pass";
        }
    };

    index.init = function() {
        index.loadTags();
        index.bind();
    };

    index.init();
})(jQuery, index);