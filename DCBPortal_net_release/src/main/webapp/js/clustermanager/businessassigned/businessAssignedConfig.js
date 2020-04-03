//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //用户展示Grid对象
    JsVar["roleGrid"] = mini.get("roleGrid");

    //初始化用户列表
    loadRole({});

});


function onTabLoad() {
    if (JsVar["roleGrid"].data.length > 0){
        var st = JsVar["roleGrid"].getRow(0);
        var params = {
            ROLE_ID:st.ROLE_ID,
            ROLE_NAME:st.ROLE_NAME
        }
        JsVar["roleGrid"].setSelected(JsVar["roleGrid"].getRow(0));
        loadRoleProgramList(params);
    }
}


/**
 * 查询用户列表
 */
function queryRoleList() {
    var roleName = mini.get("roleName").getValue();
    var params = {
        ROLE_NAME:roleName
    };
    this.loadRole(params);
}

/**
 * 加载用户列表信息
 */
function loadRole(params) {
    datagridLoadPage(JsVar["roleGrid"], params, "userProgramListMapper.queryBusRoleList");

}

/**
 * 用户行点击事件,加载业务程序列表
 * @param e
 */
function btnRoleClick(e) {
    if (!e) {
        return;
    }
    var roleId = e.record.ROLE_ID;
    var roleName = e.record.ROLE_NAME;

    var params = {
        ROLE_ID:roleId,
        ROLE_NAME:roleName
    }
    loadRoleProgramList(params);
}

/**
 * 加载程序列表信息
 */
function loadRoleProgramList(params) {
    var tabs = mini.get("#busTabs");

    //设置初始化加载Tab数据
    var iframe = tabs.getTabIFrameEl(tabs.getTab(tabs.getActiveIndex()));
    if (iframe != null && iframe.contentWindow != null) {
        iframe.contentWindow.loadPage(params);
    }

    //第一次多面板加载时触发事件，activechanged事件中iframe对象未加载完成
    tabs.on("tabload", function(e) {
        var iframe = tabs.getTabIFrameEl(e.tab);
        if (iframe != null && iframe.contentWindow != null) {
            var selRow = mini.get("roleGrid").getSelected();
            var newParams = {
                ROLE_ID:selRow.ROLE_ID,
                ROLE_NAME:selRow.ROLE_NAME
            };
            iframe.contentWindow.loadPage(newParams);
        }
    });

    //Tab事件
    tabs.on("activechanged", function(e){
        var iframe = tabs.getTabIFrameEl(e.tab);
        if (iframe != null && iframe.contentWindow != null) {
            var selRow = mini.get("roleGrid").getSelected();
            var newParams = {
                ROLE_ID:selRow.ROLE_ID,
                ROLE_NAME:selRow.ROLE_NAME
            };
            iframe.contentWindow.loadPage(newParams);
        }
    });
}