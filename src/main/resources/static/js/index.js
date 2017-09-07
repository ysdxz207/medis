var index = {
    base: $('#base').val(),
    $select:  $("#select_key")
};
(function ($, index) {
    /**
     * 获取选中的db
     * @returns {*}
     */
    index.getSelectDB = function() {
        return $('#select_db').val();
    };

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
        $.get(index.base + '/redis/get', {db: index.getSelectDB(), key: key}, function (data) {
            $('#input_key').val(key);
            $('#text_value').val(data);
        })
    };

    /**
     * 查询key
     * @param key
     */
    index.keys = function(key) {
        var $list = $('#list_key');
        $.get(index.base + "/redis/keys", {
            db: index.getSelectDB(),
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

        $.get(index.base + '/redis/delete', {db: index.getSelectDB(), key: key}, function (data) {
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
        $.get(index.base + '/redis/edit', {db: index.getSelectDB(), key: key, value: value}, function (data) {
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

        $('#btn_del').on('click', function () {
            salert('确定删除？',function(choose){
                if (choose) {
                    index.deleteRedis($('#input_key').val())
                }
            });
        });
        $('#btn_edit').on('click', function () {
            index.editRedis();
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
                $('#input_host').val('');
                $('#input_port').val('');
                $('#input_pass').val('');
                return;
            }
            var confArr = confStr.split(':');
            var host = confArr[0];
            var port = confArr[1];
            var pass = confArr[2];
            $('#input_host').val(host);
            $('#input_port').val(port);
            $('#input_pass').val(pass);
            index.saveConfAndConnect();
        });
    };

    /**
     * 保存配置并连接redis
     */
    index.saveConfAndConnect = function(){
        var host = $('#input_host').val();
        var port = $('#input_port').val();
        var pass = $('#input_pass').val();

        if (!host) {
            salert("未填入host");
            return;
        }
        if (!port) {
            salert("未填入port");
            return;
        }
        if (!pass) {
            salert("未填入pass");
            return;
        }



        $.get(index.base + "/redis/connect", {
            host: host,
            port: port,
            pass: pass
        }, function (data) {
                if (!data) {
                    salert("配置不正确！");
                }
        }, 'json');
    };

    /**
     * 删除配置
     */
    index.deleteConf = function () {
        var host = $('#input_host').val();
        var port = $('#input_port').val();
        var pass = $('#input_pass').val();

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


    index.init = function() {
        index.loadTags();
        index.bind();
    };

    index.init();
})(jQuery, index);