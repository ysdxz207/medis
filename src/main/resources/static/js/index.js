var index = {
    base: $('#base').val(),
    $selectDB: $('#select_db'),
    $inputKeyTag: $("#input_key_tag"),
    $inputHost: $('#input_host'),
    $inputPort: $('#input_port'),
    $inputPass: $('#input_pass'),
    $selectConf: $('#select_conf'),
    format: false
};
(function ($, index) {

    /**
     * 载入标签列表
     */
    index.loadTags = function () {
        var options;
        $.get(index.base + "/tag/tags", function (data) {
            options = {
                source: data,
                showHintOnFocus: 'all',
                autoSelect: false,
                fitToElement: true,
                displayText: function (item) {
                    return item.name + "[" + item.value + "]";
                },
                afterSelect: function (item) {
                    index.$inputKeyTag.val(item.value);
                    index.keys(item.value);
                }

            };
            index.$inputKeyTag.typeahead(options);


        }, 'json');

    };

    /**
     * 获取值
     * @param key
     */
    index.getValue = function (key, hkey) {
        var msg = index.validateFail();
        if (msg) {
            salert(msg);
            return;
        }
        $('#text_value').val('');
        $.get(index.base + '/redis/get', {db: index.$selectDB.val(), key: key, hkey:hkey}, function (data) {
            $('#input_key').val(key);
            if (hkey) {
                $('#input_h_key').val(hkey);
            }
            $('#text_value').val(data);
        })
    };

    /**
     * 查询key
     * @param key
     */
    index.keys = function (key) {
        var msg = index.validateFail();
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
    index.deleteRedis = function (key, hkey) {

        $.get(index.base + '/redis/delete', {db: index.$selectDB.val(), key: key, hkey: hkey}, function (data) {
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
    index.editRedis = function () {

        var key = $('#input_key').val(),
            hkey = $('#input_hkey').val(),
            value = $('#text_value').val();
        $.get(index.base + '/redis/edit', {db: index.$selectDB.val(), key: key, hkey: hkey, value: value}, function (data) {
            if (data) {
                salert('修改成功');
            } else {
                salert('修改失败');
            }
        })
    };


    index.bind = function () {
        index.$inputKeyTag.keyup(function (event) {
            if (event.keyCode == 13) {
                index.keys(index.$inputKeyTag.val());
            } else if (!this.value && event.keyCode != 38 && event.keyCode != 40) {
                index.$inputKeyTag.blur().focus();
            }

        });

        //hkey绑定事件
        $('#input_hkey').keyup(function (event) {
            if (event.keyCode == 13) {
                var key = $('#input_key').val(),
                    hkey = $('#input_hkey').val();
                index.getValue(key, hkey);
            }

        });

        //key操作
        $('#select_operation').on('change', function () {
            switch (this.value) {
                case 'delete':
                    salert('确定删除？', function (choose) {
                        if (choose) {
                            index.deleteRedis($('#input_key').val())
                        }
                    });
                    break;
                case 'edit':
                    salert('确定修改？', function (choose) {
                        if (choose) {
                            index.editRedis();
                        }
                    });
                    break;
                case 'format':
                    index.toggleFormat(this);
                    break;
            }

            $(this).val('');
        });

        //hkey操作
        $('#select_h_operation').on('change', function () {
            var key = $('#input_key').val(),
                hkey = $('#input_hkey').val();
            switch (this.value) {
                case 'query':
                    index.getValue(key, hkey);
                    break;
                case 'delete':
                    salert('确定删除？', function (choose) {
                        if (choose) {
                            index.deleteRedis(key, hkey)
                        }
                    });
                    break;
                case 'edit':
                    salert('确定修改？', function (choose) {
                        if (choose) {
                            index.editRedis();
                        }
                    });
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
    index.saveConfAndConnect = function () {

        var msg = index.validateFail();
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
            dataType: 'json',
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

        salert( '确定删除配置？', function (ok) {
            if (ok) {
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
            }
        });
    };


    /**
     * 获取数据库统计
     */
    index.count = function () {

        $.get(index.base + "/redis/count", function (data) {
            if (data) {
                index.$selectDB.empty();
                $.each(data, function (i, count) {
                    var $option = $('<option value="' + count.name.replace(/[^0-9]/ig, "") + '">'
                        + count.name + '&nbsp;&nbsp;&nbsp;&nbsp;key数量:'
                        + count.count + '</option>');
                    index.$selectDB.append($option);
                });
            }
        }, 'json');
    };

    index.toggleFormat = function (btn) {
        if (!index.format) {
            var strJson = $('#text_value').val();
            if (!strJson) {
                return;
            }
            var obj = JSON.parse(strJson);
            var str = JSON.stringify(obj, undefined, 2);
            $('#text_value').val(str);
            index.format = true;
        } else {
            index.getValue($('#input_key').val(),$('#input_hkey').val());
            index.format = false;
        }

    };

    /**
     * 校验参数
     * @returns {*}
     */
    index.validateFail = function () {
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

    /**
     * 默认配置
     */
    index.loadDefaultConf = function () {
        var $options = index.$selectConf.find('option');
        if ($options.length > 1) {
            var $option = $($options[1]);
            $option.prop('selected', true);
            index.$selectConf.trigger('change');
        }
    };

    index.init = function () {
        index.loadTags();
        index.bind();
        index.loadDefaultConf();
    };

    index.init();
})(jQuery, index);