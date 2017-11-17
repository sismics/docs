<#macro email>
  <table style="width: 100%; font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif,'Apple Color Emoji','Segoe UI Emoji','Segoe UI Symbol';">
    <tr style="background: #242424; color: #fff;">
      <td style="padding: 12px; font-size: 16px; font-weight: bold;">
        ${app_name}
      </td>
    </tr>
    <tr>
      <td style="padding-bottom: 10px; padding-top: 10px;">
        <div style="border: 1px solid #ddd; padding: 10px;">
          <#nested>
        </div>
      </td>
    </tr>
  </table>
</#macro>