/**
 * raw data converter for http://www.atimelogger.com/
 */

println 'converting aTimeLogger format'

@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv
 
assert args && args[0], 'you must provide the raw file as an argument'

file = new File(args[0])

assert file.exists(), 'input file "' + file.path + '" not available'

def lines = file.readLines()
def emptyLineIdx = lines.findIndexOf { it == '' }
if (emptyLineIdx > 0) {
  lines = lines.subList(0, emptyLineIdx)
}

def data_iterator = parseCsv(lines.join('\n'), separator: ',', readFirstLine: false)

def groupedData = data_iterator.collect({
    return [
        Date.parse('yyyy-MM-dd', it.From.replace(/\ .*/, '')), 
        it.'Activity type' + '@' + it.Comment,
        Date.parse('HH:mm', it.Duration)
    ];
}).groupBy({ it[0] }, { it[1] }).sort({ it.key })

new File('report.csv').withWriter { writer ->
    groupedData.each { dateEntry ->
        dateEntry.value.each { activityEntry ->
            def duration = groovy.time.TimeCategory.minus(
                new Date(activityEntry.value.collect({ it[2] })
                    .inject(0) { sum, item -> sum + (item.hours * 60 + item.minutes) * 60 * 1000 }),
                new Date(0)  
            )
            writer.writeLine dateEntry.key.format('dd.MM.yyyy') + ';' + activityEntry.key + ';' + duration.hours + ':' + duration.minutes.toString().padLeft(2, '0')
        }
    }
}

println 'successfully wrote "report.csv"'
