package com.tydic.bp.exception;

public enum ERRORS {
    ERR_UNKNOWN_ERROR("-10200", "Unknown error", "未知错误"),

    ERR_NO_SUCH_FILE_OR_DIR("-10201", "No such file or directory", "文件或目录不存在"),

    ERR_FILE_OR_DIR_ALREADY_EXIST("-10202", "File or directory already exist", "文件或目录已存在"),

    ERR_DIRECTORY_ISNOT_EMPTY("-10203", "Directory is not empty", "目录不为空"),

    ERR_INVALID_ARGUMENT("-10204", "Invalid argument", "无效的参数"),

    ERR_PERMISSION_DENIED("-10205", "Permission denied", "权限不足"),

    ERR_NO_AVAILABLE_TRACKER("-10206", "No available tracker", "无可用的Tracker"),

    ERR_NO_AVAILABLE_STORAGE("-10207", "No available storage", "无可用的Storage"),

    ERR_WRITE_FILE_CONFLICT("-10208", "Write file conflict", "文件上传异常，请重试"),

    ERR_REDIS_CONNECT_ERROR("-10209", "Redis connect error", "Redis连接异常"),

    ERR_REDIS_CMD_ERROR("-10210", "Redis cmd error", "Redis执行命令异常"),

    ERR_REDIS_CMD_NIL("-10211", "Redis cmd return nil", "Redis执行命令结果为空"),

    ERR_AUTH_LOGIN_ERROR("-10212", "Auth login error", "登录认证失败，用户名或密码不正确"),

    ERR_AUTH_USER_NOT_LOGIN("-10213", "User not login", "用户未登录"),

    ERR_USER_NOT_EXIST("-10214", "Uer not exist", "用户不存在"),

    ERR_USER_ALREADY_EXIST("-10215", "User already exist", "用户已存在"),

    ERR_GROUP_NOT_EXIST("-10216", "Group not exist", "用户组不存在"),

    ERR_GROUP_ALREADY_EXIST("-10217", "Group already exist", "用户组已存在"),

    ERR_JAVA_WAIT_IDLE_TIMEOUT("-10217", "Wait idle timeout", "连接超时，请重试"),

    ERR_JAVA_INVALID_CONNECTION("-10217", "Invalid connection", "连接已失效，请重试"),

    ERR_JAVA_FASTDFS_ERROR("-10217", "Fastdfs system error", "文件系统服务端错误"),

    ERR_JAVA_SYS_ERROR("-10217", "", "");

    String code;
    String message;
    String desc;

    private ERRORS(String code, String message)
    {
        this.message = message;
        this.code = code;
    }

    private ERRORS(String code, String message, String desc) {
        this.message = message;
        this.code = code;
        this.desc = desc;
    }

    public DCFileException ERROR() {
        return new DCFileException("DCFile", this.code, this.message, this.desc);
    }

    public DCFileException ERROR(String desc) {
        return new DCFileException("DCFile", this.code, this.message, desc);
    }

    public DCFileException ERROR(String message, String desc) {
        return new DCFileException("DCFile", this.code, message, this.desc);
    }

    public DCFileException ERROR(String code, String message, String desc) {
        return new DCFileException("DCFile", code, message, this.desc);
    }
}
