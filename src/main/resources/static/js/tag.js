var tag = {
    base: $('#base').val(),
    $table: $('#table_tag'),
    $selectOperation: $('#select_tag_operation'),
    $inputTagName: $('#input_tag_name'),
    $inputTagValue: $('#input_tag_value')
};
(function ($, tag) {

    tag.bind = function () {
        tag.$selectOperation.on('change', function () {
           switch (this.value) {
               case 'add':
                    tag.add();
                   break;
               case 'delete':
                    tag.delete();
                   break;
           }
           $(this).val('');
        });
    };

    tag.loadTable = function () {
        $.get(tag.base + '/tag/tags', function (data) {
            tag.$table.empty();
            $.each(data, function (i, value) {
                var $tr = $('<tr><td>' + value.name + '</td><td>' + value.value + '</td></tr>')
                    .bind('click', function () {
                        tag.fillTag(value)
                    });
                tag.$table.append($tr);

            });
        }, 'json');
    };

    tag.fillTag = function (t) {
        tag.$inputTagName.val(t.name);
        tag.$inputTagValue.val(t.value);
    };

    tag.add = function () {
        var tagName = tag.$inputTagName.val();
        var tagValue= tag.$inputTagValue.val();
        if (!tagName || !tagValue) {
            return;
        }

        $.get(tag.base + '/tag/add', {
            name: tagName,
            value: tagValue
        }, function (data) {
            tag.loadTable();
        }, 'json');

    };

    tag.delete = function() {
        var tagName = tag.$inputTagName.val();
        var tagValue= tag.$inputTagValue.val();
        if (!tagName || !tagValue) {
            return;
        }

        $.get(tag.base + '/tag/delete', {
            name: tagName,
            value: tagValue
        }, function (data) {
            if (data) {
                tag.loadTable();
            } else {
                salert("删除失败！");
            }

        }, 'json');
    };

    tag.init = function () {
        tag.loadTable();
        tag.bind();
    };

    tag.init();
})(jQuery, tag);