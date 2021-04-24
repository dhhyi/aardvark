@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv

def toClock(t) {
    int hours = t / 60; //since both are ints, you get an int
    int minutes = t % 60;
    return String.format("%d:%02d", hours, minutes);
}

def parsed = parseCsv(new File('report.csv').text, separator: ';', readFirstLine: true).collect { data ->
    def duration = Date.parse('HH:mm', data[2])
    duration.minutes = ((Math.floor((duration.minutes + 10) / 15) * 15).toInteger())

    if (duration.hours == 0 && duration.minutes < 15) {
        duration.minutes = 15
    }

    def taskData = data[1].split('@')
    def project = taskData[0]
    def task = taskData[1]

    return [data[0], project, task, duration.hours * 60 + duration.minutes]
}
.groupBy { entry -> entry[0] + " - " + entry[1] }
.collect { it -> [it.key, it.value.collect { it[3] }.sum() / 60.0, it.value.collect { it[2] }.join(', ')] }

parsed.each { println it }