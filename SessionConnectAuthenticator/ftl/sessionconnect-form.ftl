<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        ${msg("loginTitleHtml",realm.name)}
    <#elseif section = "form">
		<form id="kc-totp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                 <div class="${properties.kcLabelWrapperClass!}">
                    <label for="session_id" class="${properties.kcLabelClass!}" >A user can authorize you using the following session id:</label>
                    <input id="session_id" name="session_id" type="text" value="${(session_id)}" class="${properties.kcInputClass!} readonly="true"/>
                </div>
                <div class="${properties.kcLabelWrapperClass!}">
                	<#if session_id??> <img src="http://chart.apis.google.com/chart?cht=qr&choe=UTF-8&chs=50x50&chld=H&chl=${session_id}"> </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                    	<button class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}">Login</button>
                    </div>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout> 