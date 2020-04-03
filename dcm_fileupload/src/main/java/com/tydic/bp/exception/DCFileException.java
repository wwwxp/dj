package com.tydic.bp.exception;

public class DCFileException extends Exception{
    private static final long serialVersionUID = -1848618491499044704L;
    private String module;
    private String code;
    private String desc;

    public DCFileException(String module, String code, String message)
    {
        super(message);
        this.module = module;
        this.code = code;
    }

    public DCFileException(String module, String code, String message, String desc) {
        super(message);
        this.module = module;
        this.code = code;
        this.desc = desc;
    }

    public String getModule()
    {
        return this.module;
    }

    public String getCode()
    {
        return this.code;
    }

    public String getDesc()
    {
        return this.desc;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(": [");
        sb.append(this.module);
        sb.append("] - ");
        sb.append(this.code);
        sb.append(" - ");
        sb.append(getMessage());
        if (getDesc() != null) {
            sb.append(" - ");
            sb.append(getDesc());
        }
        return sb.toString();
    }
}
