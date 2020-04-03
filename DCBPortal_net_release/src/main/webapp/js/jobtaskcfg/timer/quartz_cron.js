/**
 *  cron表达式中的/表示循环周期, 比如秒2/2:表示从2秒钟开始，每隔2秒执行一次
 *  cron表达式中-表示指定周期，比如秒2-5：表示从2秒到5秒
 *  cron表达式中的#出现在星期中，比如2#2：表示第二周的星期二，实际上是第二周的星期一(因为cron表达式星期一是从星期日开始)
 *  cron表达式中的L表示最后，在天中表示每月最后一天，在每个月的最后一个星期几(4L，表示每个月的最后一个星期四(实际是星期三))
 *  cron表达式中的*表示匹配任何值
 *  cron表达式中的?只能用于日期和星期，表示两者互斥，没有特定的值，日期和星期有且只能一个指明为?   
 *  cron表达式小的时间单位强依赖大时间单位，比如秒依赖分钟，小时，天，月，周，年，分钟依赖小时，天，月，周，年。以此类推
 *  	比如，指定了执行的年份，周月天小时配置都受配置年份的限制，只在指定的年份执行
 */

/**
 * 初始化
 */
$(function() {
	mini.parse();
});

/**
 * 加载界面参数
 */
function onLoadComplete(data) {
	// cron表达式为空，则设置cron参数
	if (!isNull(data.cron)) {
		mini.get("cron").setValue(data.cron);
		mini.get("cron_text").setValue(data.cron_text);
		// 初始化偏移量界面数据
		resolve();
	}
}

/**
 * 不指定时间模式,设置属性为*
 */
function unAppoint(name) {
	switch (name) {
	case "second":
		mini.get("v_second").setValue("*");
		// 不指定秒，如果分钟不指定且为0的话，设置分钟为*,小时同理
		if (document.getElementById('min_unAppoint').checked) {
			mini.get("v_min").setValue("*");
		}
		if (document.getElementById('hour_unAppoint').checked) {
			mini.get("v_hour").setValue("*");
		}
		break;
	case "min":
		mini.get("v_min").setValue("*");
		// 不指定分钟，如果小时不指定且为0的话，设置小时为*
		if (document.getElementById('hour_unAppoint').checked) {
			mini.get("v_hour").setValue("*");
		}
		break;
	case "hour":
		mini.get("v_hour").setValue("*");
		break;
	case "day":
		mini.get("v_day").setValue("?");
		break;
	case "month":
		mini.get("v_month").setValue("*");
		break;
	case "week":
		// 周不指定默认为?
		mini.get("v_week").setValue("?");
		break;
	case "year":
		mini.get("v_year").setValue("");
		break;
	}
	generateCronAndText();
}

/**
 * 指定循环周期
 */
function appointCycle(name) {
	switch (name) {
	case "second":
		document.getElementById('appointCycle_second').checked = true;
		var start = mini.get("secondStart_0").getValue();
		var end = mini.get("secondEnd_0").getValue();
		var value = start + "-" + end;
		mini.get("v_second").setValue(value);
		break;
	case "min":
		document.getElementById('appointCycle_min').checked = true;
		var minStart_0 = mini.get("minStart_0").getValue();
		var minEnd_0 = mini.get("minEnd_0").getValue();
		var v_min = minStart_0 + "-" + minEnd_0;
		mini.get("v_min").setValue(v_min);
		// 指定了分钟循环执行，则需要判断秒的执行方式，如果是不指定秒执行方式，则忽略
		setV_second("0");
		break;
	case "hour":
		document.getElementById('appointCycle_hour').checked = true;
		var hourStart_0 = mini.get("hourStart_0").getValue();
		var hourEnd_0 = mini.get("hourEnd_0").getValue();
		var v_hour = hourStart_0 + "-" + hourEnd_0;
		mini.get("v_hour").setValue(v_hour);
		// 指定了小时循环执行，则需要判断秒，分钟的执行方式，如果是不指定执行方式，则忽略
		setV_second("0");
		setV_min("0");
		break;
	case "day":
		document.getElementById('appointCycle_day').checked = true;
		var dayStart_0 = mini.get("dayStart_0").getValue();
		var dayEnd_0 = mini.get("dayEnd_0").getValue();
		var v_day = dayStart_0 + "-" + dayEnd_0;
		mini.get("v_day").setValue(v_day);
		mini.get("v_week").setValue("?");
		// 指定了天循环执行，则需要判断秒，分钟，小时的执行方式，如果是不指定执行方式，则忽略
		setV_second("0");
		setV_min("0");
		setV_hour("0");
		break;
	case "month":
		document.getElementById('appointCycle_month').checked = true;
		var monthStart_0 = mini.get("monthStart_0").getValue();
		var monthEnd_0 = mini.get("monthEnd_0").getValue();
		var v_month = monthStart_0 + "-" + monthEnd_0;
		mini.get("v_month").setValue(v_month);
		break;
	case "year":
		document.getElementById('appointCycle_year').checked = true;
		var yearStart_0 = mini.get("yearStart_0").getValue();
		var yearEnd_0 = mini.get("yearEnd_0").getValue();
		var v_year = yearStart_0 + "-" + yearEnd_0;
		mini.get("v_year").setValue(v_year);
		break;
	}
	generateCronAndText();
}

/**
 * 循环执行时间模式
 */
function startOn(name, checkbox) {
	switch (name) {
	case "second":
		var cycle_second = document.getElementById('cycle_second').checked = true;
		var start = mini.get("secondStart_1").getValue();
		var end = mini.get("secondEnd_1").getValue();
		var value = start + "/" + end;
		mini.get("v_second").setValue(value);
		// 指定了秒循环执行，则需要判断分钟，小时的执行方式，如果是不指定分钟，小时执行方式，需要设置成*
		setV_min("*");
		setV_hour("*");
		break;
	case "min":
		document.getElementById('cycle_min').checked = true;
		var minStart_1 = mini.get("minStart_1").getValue();
		var minEnd_1 = mini.get("minEnd_1").getValue();
		var v_min = minStart_1 + "/" + minEnd_1;
		mini.get("v_min").setValue(v_min);
		// 指定了分钟循环执行，则需要判断秒的执行方式，如果是不指定秒执行方式，则忽略
		setV_second("0");
		// 指定了分钟循环执行，则需要判断小时的执行方式，如果是不指定小时执行方式，需要设置成*
		setV_hour("*");
		break;
	case "hour":
		document.getElementById('cycle_hour').checked = true;
		var hourStart_1 = mini.get("hourStart_1").getValue();
		var hourEnd_1 = mini.get("hourEnd_1").getValue();
		var v_hour = hourStart_1 + "/" + hourEnd_1;
		mini.get("v_hour").setValue(v_hour);

		// 指定了小时循环执行，则需要判断分钟和秒的执行方式，如果是不指定执行方式，则忽略
		setV_second("0");
		setV_min("0");
		break;
	case "day":
		document.getElementById('cycle_day').checked = true;
		var dayStart_1 = mini.get("dayStart_1").getValue();
		var dayEnd_1 = mini.get("dayEnd_1").getValue();
		var v_day = dayStart_1 + "/" + dayEnd_1;
		mini.get("v_day").setValue(v_day);
		mini.get("v_week").setValue("?");

		setV_second("0");
		setV_min("0");
		setV_hour("0");
		break;
	case "month":
		document.getElementById('cycle_month').checked = true;
		var monthStart_1 = mini.get("monthStart_1").getValue();
		var monthEnd_1 = mini.get("monthEnd_1").getValue();
		var v_month = monthStart_1 + "/" + monthEnd_1;
		mini.get("v_month").setValue(v_month);
		break;
	case "week":
		document.getElementById('cycle_week').checked = true;
		var weekStart_1 = mini.get("weekStart_1").getValue();
		var weekEnd_1 = mini.get("weekEnd_1").getValue();
		var r_weekEnd = parseInt(weekEnd_1) + 1;
		// cron表达式的星期从星期天
		if (r_weekEnd == 8)
			r_weekEnd = 1;
		var v_week = r_weekEnd + "#" + weekStart_1;
		mini.get("v_week").setValue(v_week);
		mini.get("v_day").setValue("?");
		break;

	}
	generateCronAndText();
}

/**
 * 设置秒执行的方式为0，即忽略秒执行方式,设置为*,则表示接受任何值
 */
function setV_second(value) {
	var checkValue = $('input[name="second"]:checked').val();
	if (checkValue == 'second_unAppoint') {
		mini.get("v_second").setValue(value);
	}
}

/**
 * 设置分钟执行的方式为0，即忽略分钟执行方式,设置为*,则表示接受任何值
 */
function setV_min(value) {
	var checkValue = $('input[name="min"]:checked').val();
	if (checkValue == 'min_unAppoint') {
		mini.get("v_min").setValue(value);
	}
}

/**
 * 设置小时执行的方式为0，即忽略小时执行方式,设置为*,则表示接受任何值
 */
function setV_hour(value) {
	var checkValue = $('input[name="hour"]:checked').val();
	if (checkValue == 'hour_unAppoint') {
		mini.get("v_hour").setValue(value);
	}
}

/**
 * 指定执行时间模式
 */
function appoint(name) {
	switch (name) {
	case "second":
		document.getElementById('second_appoint').checked = true;
		var second_checkbox = mini.get("second_checkbox").getValue();
		// 指定模式，如果没有选择执行分钟，则设置为*
		if (isNull(second_checkbox)) {
			mini.get("v_second").setValue("*");
		} else {
			var dataArray = second_checkbox.split(",");
			// 把数组按照order属性排序
			dataArray = dataArray.sort(function(a, b) {
				return a - b;
			});
			mini.get("v_second").setValue(dataArray.join(","));
		}

		break;
	case "min":
		document.getElementById('min_appoint').checked = true;
		var min_checkbox = mini.get("min_checkbox").getValue();
		// 指定模式，如果没有选择执行分钟，则设置为*
		if (isNull(min_checkbox)) {
			mini.get("v_min").setValue("*");
		} else {
			var dataArray = min_checkbox.split(",");
			// 把数组按照order属性排序
			dataArray = dataArray.sort(function(a, b) {
				return a - b;
			});
			mini.get("v_min").setValue(dataArray.join(","));
		}

		break;
	case "hour":
		document.getElementById('hour_appoint').checked = true;
		var hour_checkbox = mini.get("hour_checkbox").getValue();
		// 指定模式，如果没有选择执行小时，则设置为*
		if (isNull(hour_checkbox)) {
			mini.get("v_hour").setValue("*");
		} else {
			var dataArray = hour_checkbox.split(",");
			// 把数组按照order属性排序
			dataArray = dataArray.sort(function(a, b) {
				return a - b;
			});
			mini.get("v_hour").setValue(dataArray.join(","));
		}

		break;
	case "day":
		document.getElementById('day_appoint').checked = true;
		var day_checkbox = mini.get("day_checkbox").getValue();
		// 指定模式，如果没有选择执行日期，则设置为*
		if (isNull(day_checkbox)) {
			mini.get("v_day").setValue("*");
		} else {
			var dataArray = day_checkbox.split(",");
			// 把数组按照order属性排序
			dataArray = dataArray.sort(function(a, b) {
				return a - b;
			});
			mini.get("v_day").setValue(dataArray.join(","));
			mini.get("v_week").setValue("?");
		}

		break;
	case "month":
		document.getElementById('month_appoint').checked = true;
		var month_checkbox = mini.get("month_checkbox").getValue();
		// 指定模式，如果没有选择执行月份，则设置为*
		if (isNull(month_checkbox)) {
			mini.get("v_month").setValue("*");
		} else {
			var dataArray = month_checkbox.split(",");
			// 把数组按照order属性排序
			dataArray = dataArray.sort(function(a, b) {
				return a - b;
			});
			mini.get("v_month").setValue(dataArray.join(","));
		}

		break;
	case "week":
		document.getElementById('week_appoint').checked = true;
		var week_checkbox = mini.get("week_checkbox").getValue();
		// 指定模式，如果没有选择执行周，则设置为?
		if (isNull(week_checkbox)) {
			mini.get("v_week").setValue("?");
		} else {
			var dataArray = week_checkbox.split(",");
			// 把数组按照order属性排序
			dataArray = dataArray.sort(function(a, b) {
				return a - b;
			});
			mini.get("v_week").setValue(dataArray.join(","));
			// 设置了周执行模式，日期模式设置为问号
			mini.get("v_day").setValue("?");
		}

		break;
	}
	generateCronAndText();
}

/**
 * 本月最后一天
 */
function lastDay() {
	// 设置了日期执行模式，周模式设置为问号
	mini.get("v_day").setValue("L");
	mini.get("v_week").setValue("?");
	generateCronAndText();
}

/**
 * 本月最后星期几
 */
function lastWeek() {
	document.getElementById('last_week').checked = true;
	var weekStart_2 = mini.get("weekStart_2").getValue();
	var r_weekStart = parseInt(weekStart_2) + 1;
	// cron表达式的星期从星期天
	if (r_weekStart == 8)
		r_weekStart = 1;
	mini.get("v_week").setValue(r_weekStart + "L");
	// 设置了周执行模式，日期模式设置为问号
	mini.get("v_day").setValue("?");
	generateCronAndText();
}

/**
 * 生成cron表达式和描述
 */
function generateCronAndText() {
	generateCron();
	generateCronText();

	viewFiveFireTimes();
}

/**
 * 生成cron表达式
 */
function generateCron() {
	var v_second = mini.get("v_second").getValue();
	var v_min = mini.get("v_min").getValue();
	var v_hour = mini.get("v_hour").getValue();
	var v_day = mini.get("v_day").getValue();
	var v_month = mini.get("v_month").getValue();
	var v_week = mini.get("v_week").getValue();
	var v_year = mini.get("v_year").getValue();

	var cron = v_second + " " + v_min + " " + v_hour + " " + v_day + " "
			+ v_month + " " + v_week;
	if (!isNull(v_year)) {
		cron += " " + v_year;
	}
	// cron表达式赋值
	mini.get("cron").setValue(cron);
}

/**
 * 生成cron表达式描述
 */
function generateCronText() {
	var v_second = mini.get("v_second").getValue();
	var v_min = mini.get("v_min").getValue();
	var v_hour = mini.get("v_hour").getValue();
	var v_day = mini.get("v_day").getValue();
	var v_month = mini.get("v_month").getValue();
	var v_week = mini.get("v_week").getValue();
	var v_year = mini.get("v_year").getValue();

	var cron = v_second + " " + v_min + " " + v_hour + " " + v_day + " "
			+ v_month + " " + v_week + " " + v_year;
	var cron_text = "";
	if (!isNull(v_year)) {
		var year_arr = v_year.split("-");
		cron_text += "从" + year_arr[0] + "年到" + year_arr[1] + "年,";
	}
	// 月份
	if (v_month != "*") {
		if (v_month.indexOf("/") != -1) {
			var month_arr = v_month.split("/");
			cron_text += "从" + month_arr[0] + "月开始,每" + month_arr[1] + "个月,";
		} else if (v_month.indexOf("-") != -1) {
			var month_arr = v_month.split("-");
			cron_text += "从" + month_arr[0] + "月到" + month_arr[1] + "月,";
		} else {
			cron_text += "每年的" + v_month + "月,";
		}
	}
	
	// 星期
	if (v_week != "*" && v_week != "?") {
		if (v_week.indexOf("#") != -1) {
			var week_arr = v_week.split("#");
			cron_text += "每月第" + week_arr[0] + "周的";
			switch (week_arr[1]) {
			case "1":
				cron_text += "星期天,";
				break;
			case "2":
				cron_text += "星期一,";
				break;
			case "3":
				cron_text += "星期二,";
				break;
			case "4":
				cron_text += "星期三,";
				break;
			case "5":
				cron_text += "星期四,";
				break;
			case "6":
				cron_text += "星期五,";
				break;
			case "7":
				cron_text += "星期六,";
				break;
			}
		} else if (v_week.indexOf("L") != -1) {
			cron_text += "每月最后一个";
			var v_week_n = v_week.substr(0, 1);
			switch (v_week_n) {
			case "1":
				cron_text += "星期天,";
				break;
			case "2":
				cron_text += "星期一,";
				break;
			case "3":
				cron_text += "星期二,";
				break;
			case "4":
				cron_text += "星期三,";
				break;
			case "5":
				cron_text += "星期四,";
				break;
			case "6":
				cron_text += "星期五,";
				break;
			case "7":
				cron_text += "星期六,";
				break;
			}
		} else {
			cron_text += "每周";
			var week_arr = v_week.split(",");
			var v_week_text = "";
			for (var i = 0; i < week_arr.length; i++) {
				switch (week_arr[i]) {
				case "2":
					v_week_text += "星期一,";
					break;
				case "3":
					v_week_text += "星期二,";
					break;
				case "4":
					v_week_text += "星期三,";
					break;
				case "5":
					v_week_text += "星期四,";
					break;
				case "6":
					v_week_text += "星期五,";
					break;
				case "7":
					v_week_text += "星期六,";
					break;
				}
			}
			if (week_arr[0] == "1") {
				v_week_text += "星期天,";
			}

			cron_text += v_week_text;
		}
	}
	
	// 日
	if (v_day != "*" && v_day != "?") {
		if (v_day.indexOf("/") != -1) {
			var day_arr = v_day.split("/");
			cron_text += "从" + day_arr[0] + "号开始,"
			if ((mini.get("v_second").getValue() == "0"
					&& mini.get("v_min").getValue() == "0" && mini
					.get("v_hour").getValue() == "0")
					|| day_arr[1] != 1) {
				cron_text += "每隔" + day_arr[1] + "天,";
			}
		} else if (v_day.indexOf("-") != -1) {
			var day_arr = v_day.split("-");
			cron_text += "每月" + day_arr[0] + "号到" + day_arr[1] + "号,";
			if (mini.get("v_second").getValue() == "0"
					&& mini.get("v_min").getValue() == "0"
					&& mini.get("v_hour").getValue() == "0") {
				cron_text += "每天";
			}
		} else {
			// 每个月最后一天
			if (v_day == "L") {
				cron_text += "每月最后一天,";
			} else {
				cron_text += "每月的" + v_day + "号,";
			}
		}
	} else if (v_day == "*") {
		if (mini.get("v_second").getValue() == "0"
				&& mini.get("v_min").getValue() == "0"
				&& mini.get("v_hour").getValue() == "0") {
			cron_text += "每隔1天,";
		}
	}
	
	
	
	// 如果小时不指定，日选择的是循环执行模式，则小时变成0，与指定小时0点冲突，所以需要判断小时指定模式
	var checkValue = $('input[name="hour"]:checked').val();
	// 小时
	if (checkValue != 'hour_unAppoint') {
		if (v_hour.indexOf("/") != -1) {
			var hour_arr = v_hour.split("/");
			cron_text += "从" + hour_arr[0] + "点开始,";
			if ((mini.get("v_second").getValue() == "0" && mini.get("v_min")
					.getValue() == "0")
					|| hour_arr[1] != 1) {
				cron_text += "每隔" + hour_arr[1] + "小时,";
			}

		} else if (v_hour.indexOf("-") != -1) {
			var hour_arr = v_hour.split("-");
			cron_text += hour_arr[0] + "点到" + hour_arr[1] + "点,";
			if (mini.get("v_second").getValue() == "0"
					&& mini.get("v_min").getValue() == "0") {
				cron_text += "每小时";
			}
		} else {
			cron_text += "每天的" + v_hour + "点,";
		}
	} else {
		if (v_hour == "*") {
			if (mini.get("v_second").getValue() == "0"
					&& mini.get("v_min").getValue() == "0") {
				cron_text += "每隔1小时,";
			}
		}
	}

	// 如果分钟不指定，日和小时选择的是循环执行模式，则分钟变成0，与指定分钟0点冲突，所以需要判断分钟指定模式
	var checkValue = $('input[name="min"]:checked').val();
	// 分钟
	if (checkValue != 'min_unAppoint') {
		if (v_min.indexOf("/") != -1) {
			var min_arr = v_min.split("/");
			cron_text += "从第" + min_arr[0] + "分钟开始,";
			if (mini.get("v_second").getValue() == "0" || min_arr[1] != 1) {
				cron_text += "每隔" + min_arr[1] + "分钟,";
			}
		} else if (v_min.indexOf("-") != -1) {
			var min_arr = v_min.split("-");
			cron_text += "每小时" + min_arr[0] + "分到" + min_arr[1] + "分,";
			// 不指定秒，则为每分钟执行一次
			if (mini.get("v_second").getValue() == "0") {
				cron_text += "每分钟";
			}
		} else {
			cron_text += "每小时的" + v_min + "分钟,";
		}
	} else {
		if (v_min == "*" && mini.get("v_second").getValue() == "0") {
			cron_text += "每隔1分钟,";
		}
	}

	// 秒
	if (v_second != "*") {
		if (v_second.indexOf("/") != -1) {
			var second_arr = v_second.split("/");
			cron_text += "每隔" + second_arr[1] + "秒";
		}

	} else if (v_second == "*") {
		cron_text += "每隔1秒,";
	}

	cron_text += "执行一次";

	// 偏移量赋值
	mini.get("cron_text").setValue(cron_text);
}

/**
 * 加载界面设置数据
 */
function setCronData(cron) {
	var cron_arr = cron.split(" ");
	// 秒
	var second = cron_arr[0];
	// 分钟
	var min = cron_arr[1];
	// 小时
	var hour = cron_arr[2];
	// 天
	var day = cron_arr[3];
	// 月
	var month = cron_arr[4];
	// 周
	var week = cron_arr[5];
	// 年
	var year = "";
	if (cron_arr.length > 6)
		year = cron_arr[6];

	// 设置秒
	if (second == "*") {
		document.getElementById('second_unAppoint').checked = true;
		mini.get("v_second").setValue(min);
	} else if (second.indexOf("/") != -1) {
		document.getElementById('cycle_second').checked = true;
		mini.get("secondStart_1").setValue(second.split("/")[0]);
		mini.get("secondEnd_1").setValue(second.split("/")[1]);
		mini.get("v_second").setValue(second);
	} else if (second.indexOf("-") != -1) {
		document.getElementById('appointCycle_second').checked = true;
		mini.get("secondStart_0").setValue(second.split("-")[0]);
		mini.get("secondEnd_0").setValue(second.split("-")[1]);
		mini.get("v_second").setValue(second);
	} else {
		document.getElementById('second_appoint').checked = true;
		mini.get("second_checkbox").setValue(second);
		mini.get("v_second").setValue(second);
	}

	// 设置分钟
	if (min == "*") {
		document.getElementById('min_unAppoint').checked = true;
		mini.get("v_min").setValue(min);
	} else if (min.indexOf("/") != -1) {
		document.getElementById('cycle_min').checked = true;
		mini.get("minStart_1").setValue(min.split("/")[0]);
		mini.get("minEnd_1").setValue(min.split("/")[1]);
		mini.get("v_min").setValue(min);
	} else if (min.indexOf("-") != -1) {
		document.getElementById('appointCycle_min').checked = true;
		mini.get("minStart_0").setValue(min.split("-")[0]);
		mini.get("minEnd_0").setValue(min.split("-")[1]);
		mini.get("v_min").setValue(min);
	} else {
		document.getElementById('min_appoint').checked = true;
		mini.get("min_checkbox").setValue(min);
		mini.get("v_min").setValue(min);
	}
	
	// 设置小时
	if (hour == "*") {
		document.getElementById('hour_unAppoint').checked = true;
		mini.get("v_hour").setValue(hour);
	} else if (hour.indexOf("/") != -1) {
		document.getElementById('cycle_hour').checked = true;
		mini.get("hourStart_1").setValue(hour.split("/")[0]);
		mini.get("hourEnd_1").setValue(hour.split("/")[1]);
		mini.get("v_hour").setValue(hour);
	} else if (hour.indexOf("-") != -1) {
		document.getElementById('appointCycle_hour').checked = true;
		mini.get("hourStart_0").setValue(hour.split("-")[0]);
		mini.get("hourEnd_0").setValue(hour.split("-")[1]);
		mini.get("v_hour").setValue(hour);
	} else {
		document.getElementById('hour_appoint').checked = true;
		mini.get("hour_checkbox").setValue(hour);
		mini.get("v_hour").setValue(hour);
	}
	
	// 设置天
	if (day == "*") {
		document.getElementById('day_unAppoint').checked = true;
		mini.get("v_day").setValue(day);
	} else if (day == "?") {
		document.getElementById('day_unAppoint').checked = true;
		mini.get("v_day").setValue("?");
	} else if (day.indexOf("/") != -1) {
		document.getElementById('cycle_day').checked = true;
		mini.get("dayStart_1").setValue(day.split("/")[0]);
		mini.get("dayEnd_1").setValue(day.split("/")[1]);
		mini.get("v_day").setValue(day);
	} else if (day.indexOf("-") != -1) {
		document.getElementById('appointCycle_day').checked = true;
		mini.get("dayStart_0").setValue(day.split("-")[0]);
		mini.get("dayEnd_0").setValue(day.split("-")[1]);
		mini.get("v_day").setValue(day);
	} else if (day.indexOf("L") != -1) {
		document.getElementById('last_day').checked = true;
		mini.get("v_day").setValue(day);
	} else {
		document.getElementById('day_appoint').checked = true;
		mini.get("day_checkbox").setValue(day);
		mini.get("v_day").setValue(day);
	}
	
	// 设置月
	if (month == "*") {
		document.getElementById('month_unAppoint').checked = true;
		mini.get("v_month").setValue(month);
	} else if (month.indexOf("/") != -1) {
		document.getElementById('cycle_month').checked = true;
		mini.get("monthStart_1").setValue(month.split("/")[0]);
		mini.get("monthEnd_1").setValue(month.split("/")[1]);
		mini.get("v_month").setValue(month);
	} else if (month.indexOf("-") != -1) {
		document.getElementById('appointCycle_month').checked = true;
		mini.get("monthStart_0").setValue(month.split("-")[0]);
		mini.get("monthEnd_0").setValue(month.split("-")[1]);
		mini.get("v_month").setValue(month);
	} else {
		document.getElementById('month_appoint').checked = true;
		mini.get("month_checkbox").setValue(month);
		mini.get("v_month").setValue(month);
	}
	
	// 设置星期
	var weeks = [7,1,2,3,4,5,6];
	if (week == "?"  || year == "*") {
		document.getElementById('week_unAppoint').checked = true;
		mini.get("v_week").setValue(week);
	} else if (week.indexOf("#") != -1) {
		document.getElementById('cycle_week').checked = true;
		mini.get("weekStart_1").setValue(week.split("#")[1]);
		
		var index = week.split("#")[0] - 1;
		mini.get("weekEnd_1").setValue(weeks[index]);
		mini.get("v_week").setValue(week);
	} else if (week.indexOf("L") != -1) {
		document.getElementById('last_week').checked = true;
		
		var index = week.split("L")[0] - 1;
		mini.get("weekStart_2").setValue(weeks[index]);
		mini.get("v_week").setValue(week);
	} else {
		document.getElementById('week_appoint').checked = true;
		mini.get("week_checkbox").setValue(week);
		mini.get("v_week").setValue(week);
	}
	
	// 设置年
	if (year == "*" || year == "") {
		document.getElementById('year_unAppoint').checked = true;
		mini.get("v_year").setValue(year);
	} else if (isNull(year)) {
		document.getElementById('year_unAppoint').checked = true;
		mini.get("v_year").setValue("");
	} else {
		document.getElementById('appointCycle_year').checked = true;
		mini.get("yearStart_0").setValue(year.split("-")[0]);
		mini.get("yearEnd_0").setValue(year.split("-")[1]);
		mini.get("v_year").setValue(year);
	}
}

/**
 * 保存
 */
function saveCycle() {
	// 将子界面的值带回父界面
	var resultObject = {};
	resultObject.cron = mini.get("cron").getValue();
	if(resultObject.cron){
		setCronData(resultObject.cron)
		generateCronText();
	}
	resultObject.cron_text = mini.get("cron_text").getValue();
	resultObject.action = "success";
	closeWindow(resultObject);
}

/**
 * 
 */
/*
 * function viewFiveFireTimes(){ var param={};
 * param.cron=mini.get("cron").getValue(); showDialog("查看最近5次执行时间", 400,
 * 400,Globals.baseJspUrl.PARAMETER_QUARTZ_CRON_VIEW_FIVE_TIMES_URL,function
 * destroy(data){ }, param); }
 */

/**
 * 查看最近5次执行时间
 * 
 * @returns
 */
function viewFiveFireTimes() {
	var param = {};
	param.cron = mini.get("cron").getValue();
	datagridLoad(
			mini.get("fireTimeGrid"),
			param,
			"",
			Globals.baseActionUrl.PARAMETER_COLLECT_SELECT_CRON_FIVE_FIRE_ACTION_URL);
}

/**
 * 反向解析cron字符串到界面组件
 * 
 * @returns
 */
function resolve() {
	var cron = mini.get("cron").getValue().trim();
	if (cron == "") {
		return;
	} else {
		setCronData(cron);
		generateCronText();
		viewFiveFireTimes();
	}
}