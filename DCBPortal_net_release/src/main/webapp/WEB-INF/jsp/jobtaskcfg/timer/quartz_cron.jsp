<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<%@ include file="/public/common/common.jsp"%>
		<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />

		<script language="javascript" type="text/javascript" src="${ctx}/js/jobtaskcfg/timer/quartz_cron.js"></script>
		<title>偏移量设置</title>
	</head>

	<body>
		<div class="mini-fit p5">
			<div id="tabs1" class="mini-tabs" activeIndex="0">
				<div class="mini-fit" title="秒">
					<table class="formTable9" style="table-layout: fixed;height: 200px;">
						<colgroup>
							<col />
						</colgroup>
						<tr>
							<td>
								<input type="radio" name="second" onclick="unAppoint('second')" value="second_unAppoint" id="second_unAppoint" /> 不指定，允许的通配符[, - * /]
							</td>
						</tr>

						<tr>
							<td>
								<input type="radio" name="second" onclick="appointCycle('second')" value="appointCycle_second" id="appointCycle_second" /> 周期从
								<input class="mini-spinner" style="width: 100px;" minValue="0" maxValue="58" value="0" onvaluechanged="appointCycle('second')" id="secondStart_0" /> -
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="59" value="1" onvaluechanged="appointCycle('second')" id="secondEnd_0" /> 秒
							</td>
						</tr>

						<tr>
							<td>
								<input type="radio" name="second" onclick="startOn('second')" checked="checked" value="cycle_second" id="cycle_second" /> 从
								<input class="mini-spinner" style="width: 100px;" minValue="0" maxValue="59" value="0" onvaluechanged="startOn('second')" id="secondStart_1" /> 秒开始，每
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="59" value="1" onvaluechanged="startOn('second')" id="secondEnd_1" /> 秒执行一次
							</td>
						</tr>

						<tr>
							<td>
								<input type="radio" name="second" onclick="appoint('second')" value="second_appoint" id="second_appoint" /> 指定秒
								<input id="second_checkbox" name="second_checkbox" multiSelect="true" data="getSysDictData('second_list')" onvaluechanged="appoint('second')" showNullItem="false" repeatItems="12" repeatLayout="table" class="mini-checkboxlist" textField="text" valueField="id" style="margin-left:40px;width:100%;" />
							</td>
						</tr>
					</table>
				</div>

				<div title="分钟">
					<table class="formTable9" style="table-layout: fixed;height: 200px;">
						<colgroup>
							<col />
						</colgroup>
						<tr>
							<td>
								<input type="radio" name="min" onclick="unAppoint('min')" checked="checked" value="min_unAppoint" id="min_unAppoint" /> 不指定，允许的通配符[, - * /]
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="min" onclick="appointCycle('min')" value="appointCycle_min" id="appointCycle_min" /> 周期从
								<input class="mini-spinner" style="width: 100px;" minValue="0" maxValue="58" value="0" onvaluechanged="appointCycle('min')" id="minStart_0" /> 分钟到
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="59" value="1" onvaluechanged="appointCycle('min')" id="minEnd_0" /> 分钟
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="min" onclick="startOn('min')" value="cycle_min" id="cycle_min" /> 从
								<input class="mini-spinner" style="width: 100px;" minValue="0" maxValue="59" value="0" onvaluechanged="startOn('min')" id="minStart_1" /> 分钟开始,每
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="59" value="1" onvaluechanged="startOn('min')" id="minEnd_1" /> 分钟执行一次
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="min" id="min_appoint" value="min_appoint" onclick="appoint('min')" /> 指定分钟
								<input id="min_checkbox" name="min_checkbox" multiSelect="true" data="getSysDictData('min_list')" onvaluechanged="appoint('min')" showNullItem="false" repeatItems="12" repeatLayout="table" class="mini-checkboxlist" textField="text" valueField="id" style="margin-left:40px;width:100%;" />
							</td>
						</tr>
					</table>
				</div>
				<div title="小时">
					<table class="formTable9" style="table-layout: fixed;height: 200px;">
						<colgroup>
							<col />
						</colgroup>
						<tr>
							<td>
								<input type="radio" name="hour" onclick="unAppoint('hour')" checked="checked" value="hour_unAppoint" id="hour_unAppoint" /> 不指定，允许的通配符[, - * /]
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="hour" onclick="appointCycle('hour')" value="appointCycle_hour" id="appointCycle_hour" /> 周期从
								<input class="mini-spinner" style="width: 100px;" minValue="0" maxValue="23" value="0" onvaluechanged="appointCycle('hour')" id="hourStart_0" /> 时到
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="23" value="1" onvaluechanged="appointCycle('hour')" id="hourEnd_0" /> 时
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="hour" onclick="startOn('hour')" value="cycle_hour" id="cycle_hour" /> 从
								<input class="mini-spinner" style="width: 100px;" minValue="0" maxValue="23" value="0" onvaluechanged="startOn('hour')" id="hourStart_1" /> 时开始,每
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="23" value="1" onvaluechanged="startOn('hour')" id="hourEnd_1" /> 小时执行一次
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="hour" id="hour_appoint" value="hour_appoint" onclick="appoint('hour')" /> 指定小时
								<input id="hour_checkbox" name="hour_checkbox" multiSelect="true" data="getSysDictData('hour_list')" onvaluechanged="appoint('hour')" showNullItem="false" repeatItems="8" repeatLayout="table" class="mini-checkboxlist" textField="text" valueField="id" style="margin-left:40px;width:100%;" />
							</td>
						</tr>
					</table>
				</div>
				<div title="日">
					<table class="formTable9" style="table-layout: fixed;height: 200px;">
						<colgroup>
							<col />
						</colgroup>
						<tr>
							<td>
								<input type="radio" name="day" onclick="unAppoint('day')" checked="checked" value="day_unAppoint" id="day_unAppoint" /> 不指定，通配符[, - * / L W]
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="day" onclick="appointCycle('day')" value="appointCycle_day" id="appointCycle_day" /> 周期从
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="31" value="1" onvaluechanged="appointCycle('day')" id="dayStart_0" /> 号到
								<input class="mini-spinner" style="width: 100px;" minValue="2" maxValue="31" value="2" onvaluechanged="appointCycle('day')" id="dayEnd_0" /> 号
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="day" onclick="startOn('day')" value="cycle_day" id="cycle_day" /> 从
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="31" value="1" onvaluechanged="startOn('day')" id="dayStart_1" /> 号开始,每
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="31" value="1" onvaluechanged="startOn('day')" id="dayEnd_1" /> 天执行一次
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="day" onclick="lastDay()" value="last_day" id="last_day" /> 本月最后一天
							</td>
						</tr>
						<tr>
							<td>
								<input type="radio" name="day" id="day_appoint" value="day_appoint" onclick="appoint('day')" /> 指定日期
								<input id="day_checkbox" name="day_checkbox" multiSelect="true" data="getSysDictData('day_list')" onvaluechanged="appoint('day')" showNullItem="false" repeatItems="8" repeatLayout="table" class="mini-checkboxlist" textField="text" valueField="id" style="margin-left:40px;width:100%;" />
							</td>
						</tr>
					</table>
				</div>
				<div title="月">
					<table class="formTable9" style="table-layout: fixed;height: 200px;">
						<colgroup>
							<col />
						</colgroup>
						<tr>
							<td>
								<input type="radio" name="month" onclick="unAppoint('month')" checked="checked" value="month_unAppoint" id="month_unAppoint" /> 不指定，允许的通配符[, - * /]
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="month" onclick="appointCycle('month')" value="appointCycle_month" id="appointCycle_month" /> 周期从
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="12" value="1" onvaluechanged="appointCycle('month')" id="monthStart_0" /> 月到
								<input class="mini-spinner" style="width: 100px;" minValue="2" maxValue="12" value="2" onvaluechanged="appointCycle('month')" id="monthEnd_0" /> 月
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="month" onclick="startOn('month')" value="cycle_month" id="cycle_month" /> 从
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="12" value="1" onvaluechanged="startOn('month')" id="monthStart_1" /> 月开始,每
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="12" value="1" onvaluechanged="startOn('month')" id="monthEnd_1" /> 月执行一次
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="month" id="month_appoint" value="month_appoint" onclick="appoint('month')" /> 指定月份
								<input id="month_checkbox" name="month_checkbox" multiSelect="true" data="getSysDictData('month_list')" onvaluechanged="appoint('month')" showNullItem="false" repeatItems="6" repeatLayout="table" class="mini-checkboxlist" textField="text" valueField="id" style="margin-left:40px;width:100%;" />
							</td>
						</tr>
					</table>
				</div>
				<div title="周">
					<table class="formTable9" style="table-layout: fixed;height: 200px;">
						<colgroup>
							<col />
						</colgroup>
						<tr>
							<td>
								<input type="radio" name="week" onclick="unAppoint('week')" checked="checked" value="week_unAppoint" id="week_unAppoint" /> 不指定，通配符[, - * / L #]
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="week" onclick="startOn('week')" value="cycle_week" id="cycle_week" /> 第
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="4" value="1" onvaluechanged="startOn('week')" id="weekStart_1" /> 周 的星期
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="7" value="1" onvaluechanged="startOn('week')" id="weekEnd_1" />执行
							</td>
						</tr>
						<tr>
							<td>
								<input type="radio" name="week" onclick="lastWeek()" value="last_week" id="last_week" /> 每月最后一个星期
								<input class="mini-spinner" style="width: 100px;" minValue="1" maxValue="7" value="1" onvaluechanged="lastWeek()" id="weekStart_2" />执行
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="week" id="week_appoint" value="week_appoint" onclick="appoint('week')" /> 指定星期
								<input id="week_checkbox" name="week_checkbox" multiSelect="true" data="getSysDictData('week_list')" onvaluechanged="appoint('week')" showNullItem="false" repeatItems="7" class="mini-checkboxlist" textField="text" valueField="id" style="margin-left:40px;width:100%;" />
							</td>
						</tr>
					</table>
				</div>
				<div title="年">
					<table class="formTable9" style="table-layout: fixed;height: 200px;">
						<colgroup>
							<col />
						</colgroup>
						<tr>
							<td>
								<input type="radio" name="year" onclick="unAppoint('year')" checked="checked" value="year_appoint" id="year_unAppoint" /> 不指定，通配符[, - * /] 非必填
							</td>
						</tr>
						<tr>
							<td> <input type="radio" name="year" onclick="appointCycle('year')" value="appointCycle_year" id="appointCycle_year" /> 周期从
								<input class="mini-spinner" style="width: 100px;" minValue="2013" value="2016" onvaluechanged="appointCycle('year')" id="yearStart_0" /> -
								<input class="mini-spinner" style="width: 100px;" minValue="2013" value="2020" onvaluechanged="appointCycle('year')" id="yearEnd_0" />年
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div class="mini-panel" style="width:100%;padding-top: 2px;" title="表达式">
				<table class="formTable9" style="table-layout: fixed;height: 100%;">
					<tbody>
						<tr style="margin-left:120px">
							<td align="center" width="100px">
							</td>
							<td align="center">
								秒
							</td>
							<td align="center">
								分钟
							</td>
							<td align="center">
								小时
							</td>
							<td align="center">
								日
							</td>
							<td align="center">
								月
							</td>
							<td align="center">
								星期
							</td>
							<td align="center">
								年
							</td>
						</tr>
						<tr>
							<td>
								表达式字段:
							</td>
							<td>
								<input class="mini-textbox" id="v_second" name="v_second" value="0/1" readonly="readonly" width="100px" />
							</td>
							<td>
								<input class="mini-textbox" id="v_min" name="v_min" value="*" readonly="readonly" width="100px" />
							</td>
							<td>
								<input class="mini-textbox" id="v_hour" name="v_hour" value="*" readonly="readonly" width="100px" />
							</td>
							<td>
								<input class="mini-textbox" id="v_day" name="v_day" value="*" readonly="readonly" width="100px" />
							</td>
							<td>
								<input class="mini-textbox" id="v_month" name="v_month" value="*" readonly="readonly" width="100px" />
							</td>
							<td>
								<input class="mini-textbox" id="v_week" name="v_week" value="?" readonly="readonly" width="100px" />
							</td>
							<td>
								<input class="mini-textbox" id="v_year" name="v_year" readonly="readonly" width="100px" />
							</td>
						</tr>
						<tr>
							<th>Cron表达式:</th>
							<td colspan="6">
								<input class="mini-textbox" id="cron" name="cron" style="width: 100%;" value="0/1 * * * * ?" onvaluechanged="resolve" />
							</td>
							<td>
								<input class="mini-button" id="resolve" style="width: 100%;" text="解析" onclick="resolve" />
							</td>
						</tr>
						<tr style="display:none;">
							<th>当前偏移量:</th>
							<td colspan="6">
								<input class="mini-textbox" id="cron_text" name="cron_text" style="width: 90%;" value="每1秒执行一次" readonly="readonly" />
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="mini-fit" style="margin-top: 2px;">
				<div class="mini-panel" title="最近5次执行时间" style="width: 39%; height: 100%;float:left;" showToolbar="false" showCloseButton="false" showFooter="false" showPageSize="false">
					<div id="fireTimeGrid" class="mini-datagrid" style="width: 100%; height: 100%;" showHeader="false" showPager="false" showFooter="false">
						<div property="columns">
							<div field="FIRE_TIME" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="100%">执行时间</div>
						</div>
					</div>
				</div>
				<div style="width: 1%;height: 100%;float: left;"></div>
				<div class="mini-panel" title="示例" style="width: 60%; height: 100%;float:left;" showToolbar="false" showCloseButton="false" showFooter="false">
					<font color='blue' style="width: 100%; height: 100%;">
						<span>每日0点：</span><a href='#' onclick="mini.get('cron').setValue('0 0 0 * * ? ');resolve()">0 0 0 * * ?</a><br />
						<span>每周一0点：</span><a href='#' onclick="mini.get('cron').setValue('0 0 0 ? * 2 ');resolve()">0 0 0 ? * 2</a><span>(注:1=周日,2=周一,3=周二,4=周三,5=周四,6=周五,7=周六)</span><br />
						<span>每月1日0点：</span><a href='#' onclick="mini.get('cron').setValue('0 0 0 1 * ? ');resolve()">0 0 0 1 * ?</a><br />
						<span>每年1月1日0点：</span><a href='#' onclick="mini.get('cron').setValue('0 0 0 1 1 ? ');resolve()">0 0 0 1 1 ?</a><br />
						<span>2046年8月1日0点：</span><a href='#' onclick="mini.get('cron').setValue('0 0 0 1 8 ? 2046');resolve()">0 0 0 1 8 ? 2046</a>
					</font>
				</div>
			</div>
		</div>
		<div class="mini-toolbar" style="height:28px;text-align: center; padding-bottom: 8px;" borderStyle="border:0;border-top:solid 1px #b1c3e0;">
			<a class="mini-button" onclick="saveCycle()" style="margin-right: 20px;">保存</a>
			<span style="display: inline-block; width: 25px;"></span>
			<!-- <a class="mini-button" onclick="viewFiveFireTimes()" style="margin-right: 20px;">查看最近5次执行时间</a>
	    <span style="display: inline-block; width: 25px;"></span>  -->
			<a class="mini-button" onclick="closeWindow()">取消</a>
		</div>
	</body>

</html>