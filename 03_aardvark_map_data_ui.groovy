import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import javax.swing.*
import static java.awt.EventQueue.invokeLater

def rules = new groovy.json.JsonSlurper().parseText(new File('rules.json').text)
def config = new groovy.json.JsonSlurper().parseText(new File('config.json').text)

new SwingBuilder().edt {
  frame(title: 'Java Frame', size: [1000, 600], locationRelativeTo: null, show: true, defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE) {
    menuBar {
      menuItem(id:'scripts', text: 'Add Rule', actionPerformed: { 
        rules.add([:])
        rulesTable.model.fireTableDataChanged()
        rulesTable.setRowSelectionInterval(rules.size() - 1, rules.size() - 1)
      })
    }  

    gridLayout(cols: 1, rows: 2)

    panel {
      borderLayout()
      scrollPane(constraints:CENTER) {
        table(id: 'rulesTable', selectionMode: ListSelectionModel.SINGLE_SELECTION) {
          tableModel(list:rules) {
            closureColumn(header:'match', read:{row -> return row.match})
            closureColumn(header:'group', read:{row -> return row.group})
            closureColumn(header:'task', read:{row -> return row.task})
            closureColumn(header:'activity', read:{row -> return row.activity})
            closureColumn(header:'comment', read:{row -> return row.comment})
          }
        }
      }
    }

    def selectedRule = { rules.find({ it.is(rulesTable.selectedElement) }) }

    panel(visible: bind { rulesTable.selectedElement != null }) {
      gridLayout(cols: 2, rows: 5)
      
      label(text: 'Match: ', horizontalAlignment: SwingConstants.RIGHT)
      matchInput = textField(text: bind { rulesTable.selectedElement?.match })
      matchInput.addCaretListener { selectedRule()?.match = matchInput.text }

      def getGroups = { config.tasks.keySet() as List }
      label(text: 'Group: ', horizontalAlignment: SwingConstants.RIGHT)
      groupSelect = comboBox(items: getGroups(), selectedItem: bind { rulesTable.selectedElement?.group })
      groupSelect.addItemListener { selectedRule()?.group = groupSelect.selectedItem }
      
      label(text: 'Task: ', horizontalAlignment: SwingConstants.RIGHT)
      taskSelect = comboBox()
      def taskSelectUpdate = { 
        def list = config.tasks.find { key, value -> key =~ groupSelect.selectedItem }?.value ?: []
        taskSelect.model = new DefaultComboBoxModel(list.toArray())
        taskSelect.model.selectedItem = list.find { it =~ rulesTable.selectedElement?.task }
      }
      groupSelect.addItemListener taskSelectUpdate
      rulesTable.selectionModel.addListSelectionListener taskSelectUpdate
      taskSelect.addItemListener { 
        if (taskSelect.selectedItem != null)
          selectedRule().task = taskSelect.selectedItem
      }
      
      label(text: 'Activity: ', horizontalAlignment: SwingConstants.RIGHT)
      activitySelect = comboBox(items: config.activities, selectedItem: bind { rulesTable.selectedElement?.activity })
      activitySelect.addItemListener { selectedRule()?.activity = activitySelect.selectedItem }
      
      label(text: 'Comment: ', horizontalAlignment: SwingConstants.RIGHT)
      commentInput = textField(text: bind { rulesTable.selectedElement?.comment })
      commentInput.addCaretListener { selectedRule()?.comment = commentInput.text }
    }
  }
}
