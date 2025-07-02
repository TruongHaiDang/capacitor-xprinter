package com.posprinter.printdemo.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.posprinter.printdemo.App
import com.posprinter.printdemo.R
import com.posprinter.printdemo.databinding.ActivityCpclBinding
import com.posprinter.printdemo.utils.UIUtils
import net.posprinter.CPCLConst
import net.posprinter.CPCLPrinter

/**
 * @author: star
 * @date: 2022-05-27
 */
class CpclActivity : AppCompatActivity() {
    private val printer = CPCLPrinter(App.get().curConnect)
    private lateinit var bind: ActivityCpclBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCpclBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initClick()
    }

     private fun initClick() {
        bind.textBtn.setOnClickListener {
            printer.initializePrinter(300, 1)
                .addText(10, 10, CPCLConst.ROTATION_0, CPCLConst.FNT_0, "FONT0")
                .addText(100, 10, CPCLConst.ROTATION_0, CPCLConst.FNT_1, "FONT1")
                .addText(200, 10, CPCLConst.ROTATION_0, CPCLConst.FNT_2, "FONT2")
                .addText(10, 60, CPCLConst.ROTATION_0, CPCLConst.FNT_4, "FONT4")
                .addText(400, 200, CPCLConst.ROTATION_90, CPCLConst.FNT_4, "R 90")
                .addText(400, 200, CPCLConst.ROTATION_180, CPCLConst.FNT_4, "R 180")
                .addText(400, 200, CPCLConst.ROTATION_270, CPCLConst.FNT_4, "R 270")
                .addText(100, 60, CPCLConst.ROTATION_0, CPCLConst.FNT_5, "FONT5")
                .addText(200, 60, CPCLConst.ROTATION_0, CPCLConst.FNT_6, "FONT6")
                .addText(10, 120, CPCLConst.ROTATION_0, CPCLConst.FNT_7, "FONT7")
                .addText(100, 120, CPCLConst.ROTATION_0, CPCLConst.FNT_24, "FONT24")
                .setMag(2, 2)
                .addText(200, 120, CPCLConst.ROTATION_0, CPCLConst.FNT_55, "FONT55 Double")
                .addPrint()
        }
        bind.printerStatusBtn.setOnClickListener {
            printer.printerStatus(1000) {
                val str = when (it) {
                    0 -> "normal"
                    1 -> "Head opened"
                    2 -> "Paper Jam"
                    3 -> "Paper Jam and head opened"
                    4 -> "Out of paper"
                    5 -> "Out of paper and head opened"
                    8 -> "Out of ribbon"
                    9 -> "Out of ribbon and head opened"
                    10 -> "Out of ribbon and paper jam"
                    11 -> "Out of ribbon, paper jam and head opened"
                    12 -> "Out of ribbon and out of paper"
                    13 -> "Out of ribbon, out of paper and head opened"
                    16 -> "Pause"
                    32 -> "Printing"
                    else -> "Other error"
                }
                UIUtils.toast(str)
            }
        }
        bind.qrcodeBtn.setOnClickListener {
            printer.initializePrinter(400)
                .addPageWidth(480)
                .addBox(10, 10, 510, 280, 5)
                .addLine(20, 180, 510, 180, 4)
                .addQRCode(20, 20, "QR CODE ABC123")
                .addAlign(CPCLConst.ALIGNMENT_CENTER)
                .addText(180, 20, "REVERSE")
                .addInverseLine(90, 20, 190, 20, 24)
                .addAlign(CPCLConst.ALIGNMENT_LEFT)
                .addEGraphics(180, 50, 110, BitmapFactory.decodeResource(resources, R.drawable.nv_test))
                .addCGraphics(300, 50, 110, BitmapFactory.decodeResource(resources, R.drawable.nv_test))
                .addText(20, 190, "Hello World!")
                .addPrint()
        }
        bind.barcodeBtn.setOnClickListener {
            printer.initializePrinter(800)
                .addBarcodeText()
                .addText(0, 0, "Code 128")
                .addBarcode(0, 30, CPCLConst.BCS_128, 1, CPCLConst.BCS_RATIO_1, 50, "123456789")
                .addText(0, 120, "UPC-E")
                .addBarcode(0, 150, CPCLConst.BCS_UPCE, 50, "223456")
                .addText(0, 240, "EAN/JAN-13")
                .addBarcode(0, 270, CPCLConst.BCS_EAN13, 50, "323456791234")
                .addText(0, 360, "Code 39")
                .addBarcode(0, 390, CPCLConst.BCS_39, 50, "72233445")
                .addText(250, 0, "UPC-A")
                .addBarcode(250, 30, CPCLConst.BCS_UPCA, 50, "423456789012")
                .addText(250, 120, "EAN/JAN-8")
                .addBarcode(250, 150, CPCLConst.BCS_EAN8, 50, "52233445")
                .addText(300, 360, "CODABAR")
                .addBarcodeV(300, 540, CPCLConst.BCS_CODABAR, 50, "A67859B")
                .addText(0, 480, "Code 93/Ext.93")
                .addBarcode(0, 510, CPCLConst.BCS_93, 50, "823456789")
                .addBarcodeTextOff()
                .addPrint()
        }
    }


}