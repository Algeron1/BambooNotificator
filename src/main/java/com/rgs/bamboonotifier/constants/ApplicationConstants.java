package com.rgs.bamboonotifier.constants;

public class ApplicationConstants {

    public static final String SUCCESS_STATUS = "SUCCESS";
    public static final String IN_PROGRESS_STATUS = "IN_PROGRESS";
    public static final String UNKNOWN_STATUS = "UNKNOWN";

    public static final String NEW_DEPLOY_MESSAGE_TEMPLATE = """
    <b>**Новый деплой на стенде %s**</b>
    
    📦 Версия: %s
    🕓 Время начала: %s
    🕔 Время завершения: %s
    👨‍💻 Автор: %s
    🌿 Бранч: %s
    📊 Конечный статус: %s
    📊 Статус деплоя: %s
    """;

    public static final String SUCCESS_DEPLOY_MESSAGE_TEMPLATE = """
    <b>**Успешно завершен %s**</b>
    
    📦 Версия: %s
    🕓 Время начала: %s
    🕔 Время завершения: %s
    👨‍💻 Автор: %s
    🌿 Бранч: %s
    📊 Конечный статус: %s
    📊 Статус деплоя: %s
    """;

    public static final String ERROR_DEPLOY_MESSAGE_TEMPLATE = """
    <b>**Упал с ошибкой %s**</b>
    
    📦 Версия: %s
    🕓 Время начала: %s
    🕔 Время завершения: %s
    👨‍💻 Автор: %s
    🌿 Бранч: %s
    📊 Конечный статус: %s
    📊 Статус деплоя: %s
    """;

    public static final String DEPLOY_BAN_MESSAGE_TEMPLATE = """
    <b>**Просьба не деплоить %s**</b>
    
    📦 Причина: %s
    🕓 Время начала: %s
    🕔 Время завершения: %s
    👨‍💻 Автор: %s
    """;
}
