<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
        version="1.0"
        xmlns="http://schemas.microsoft.com/wix/2006/wi"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:wix="http://schemas.microsoft.com/wix/2006/wi"
        xmlns:str="http://xsltsl.org/string"
        exclude-result-prefixes="wix str"
>
    <xsl:output encoding="utf-8" method="xml" version="1.0" indent="yes"/>

    <!-- Add the necessary include file at the top to resolve variables -->
    <xsl:template match='wix:Wix'>
        <xsl:copy>
            <xsl:processing-instruction name="include">variables.wxi</xsl:processing-instruction>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='wix:Component[contains(wix:File/@Source, "$(var.g_jpkg)\lsml.exe")]'>
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <!-- Elsewhere, have an Icon element like: <Icon Id="Prog.exe" SourceFile="$(var.BUILDCACHE)Bin/Prog.exe" />  -->
            <xsl:text>&#xa;</xsl:text>
            <Shortcut Id="shtct_dtp_exe" Directory="DesktopFolder" Name="$(var.g_fname)" Icon="lsml.ico" IconIndex="0"
                      Advertise="yes">
                <xsl:text>&#xa;</xsl:text>
                <ShortcutProperty Key="System.AppUserModel.ID" Value="lisong_mechlab.view.LSML"/>
                <xsl:text>&#xa;</xsl:text>
            </Shortcut>
            <xsl:text>&#xa;</xsl:text>
            <Shortcut Id="shtct_pmd_exe" Directory="ProgramMenuFolder" Name="$(var.g_fname)" Icon="lsml.ico"
                      IconIndex="0" Advertise="yes">
                <xsl:text>&#xa;</xsl:text>
                <ShortcutProperty Key="System.AppUserModel.ID" Value="lisong_mechlab.view.LSML"/>
                <xsl:text>&#xa;</xsl:text>
            </Shortcut>
            <xsl:text>&#xa;</xsl:text>
            <RegistryKey Id="reg_hkcr_lsml" Root="HKCR" Key="lsml" ForceCreateOnInstall="yes"
                         ForceDeleteOnUninstall="yes">
                <xsl:text>&#xa;</xsl:text>
                <RegistryValue Type="string" Name="URL Protocol" Value=""/>
                <xsl:text>&#xa;</xsl:text>
            </RegistryKey>
            <xsl:text>&#xa;</xsl:text>
            <RegistryKey Id="reg_hkcr_lsml_defaulticon" Root="HKCR" Key="lsml\DefaultIcon" ForceCreateOnInstall="yes"
                         ForceDeleteOnUninstall="yes">
                <xsl:text>&#xa;</xsl:text>
                <RegistryValue Type="string" Value="{concat('[#', wix:File/@Id, '],1')}"/>
                <xsl:text>&#xa;</xsl:text>
            </RegistryKey>
            <xsl:text>&#xa;</xsl:text>
            <RegistryKey Id="reg_hkcr_lsml_command" Root="HKCR" Key="lsml\shell\open\command" ForceCreateOnInstall="yes"
                         ForceDeleteOnUninstall="yes">
                <xsl:text>&#xa;</xsl:text>
                <RegistryValue Type="string">
                    <xsl:attribute name="Value">
                        <xsl:text>"[#</xsl:text>
                        <xsl:value-of select="wix:File/@Id"/>
                        <xsl:text>]" "%1"</xsl:text>
                    </xsl:attribute>
                </RegistryValue>
                <xsl:text>&#xa;</xsl:text>
            </RegistryKey>
            <xsl:text>&#xa;</xsl:text>
            <RegistryKey Id="reg_hklm_lsml_install" ForceCreateOnInstall="yes" ForceDeleteOnUninstall="yes"
                         Key="SOFTWARE\$(var.g_mfg)\$(var.g_sname)" Root="HKLM">
                <xsl:text>&#xa;</xsl:text>
                <RegistryValue Id="FoobarRegInstallDir" Type="string" Name="InstallDir" Value="[INSTALLDIR]"/>
                <xsl:text>&#xa;</xsl:text>
            </RegistryKey>
            <xsl:text>&#xa;</xsl:text>
        </xsl:copy>
    </xsl:template>

    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>