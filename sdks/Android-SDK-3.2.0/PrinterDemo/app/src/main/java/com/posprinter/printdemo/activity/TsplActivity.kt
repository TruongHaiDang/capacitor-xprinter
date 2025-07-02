package com.posprinter.printdemo.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.posprinter.printdemo.App
import com.posprinter.printdemo.R
import com.posprinter.printdemo.databinding.ActivityTsplBinding
import com.posprinter.printdemo.utils.UIUtils
import net.posprinter.TSPLConst
import net.posprinter.TSPLPrinter
import net.posprinter.model.AlgorithmType

class TsplActivity : AppCompatActivity() {
    private val printer = TSPLPrinter(App.get().curConnect)
    private lateinit var bind: ActivityTsplBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityTsplBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initListener()
    }

    private fun initListener() {
        bind.content.setOnClickListener {
            printContent()
        }
        bind.tspltext.setOnClickListener {
            printText()
        }
        bind.tsplbarcode.setOnClickListener {
            printBarcode()
        }
        bind.tsplpic.setOnClickListener {
            printPic()
        }
        bind.bmpCompressionBtn.setOnClickListener {
            val mOptions = BitmapFactory.Options()
            mOptions.inScaled = false
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.test, mOptions)
            printer.sizeMm(76.0, 300.0)
                .gapMm(2.0, 0.0)
                .cls()
                .bitmapCompression(0, 0, TSPLConst.BMP_MODE_OVERWRITE_C, 600, bmp, AlgorithmType.Threshold)
                .print(1)
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
    }

    private fun printPic() {
        val mOptions = BitmapFactory.Options()
        mOptions.inScaled = false
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.test, mOptions)
        printPicCode(bmp)
    }

    /*
    print the text ,line and barcode
     */
    private fun printContent() {
        printer.sizeMm(60.0, 30.0)
            .gapInch(0.0, 0.0)
            .offsetInch(0.0)
            .speed(5.0)
            .density(10)
            .direction(TSPLConst.DIRECTION_FORWARD)
            .reference(20, 0)
            .cls()
            .box(6, 6, 378, 229, 5)
            .box(16, 16, 360, 209, 5)
            .barcode(30, 30, TSPLConst.CODE_TYPE_93, 100, TSPLConst.READABLE_LEFT, TSPLConst.ROTATION_0, 2, 2, "ABCDEFGH")
            .qrcode(265, 30, TSPLConst.EC_LEVEL_H, 4, TSPLConst.QRCODE_MODE_MANUAL, TSPLConst.ROTATION_0, "test qrcode")
            .text(200, 144, TSPLConst.FNT_16_24, TSPLConst.ROTATION_0, 1, 1, "Test EN")
            .text(38, 165, TSPLConst.FNT_16_24, TSPLConst.ROTATION_0, 1, 2, "HELLO")
            .bar(200, 183, 166, 30)
            .bar(334, 145, 30, 30)
            .print(1)
    }

    private fun printText() {
        printer.sizeMm(60.0, 30.0)
            .density(10)
            .reference(0, 0)
            .direction(TSPLConst.DIRECTION_FORWARD)
            .cls()
            .text(10, 10, TSPLConst.FNT_8_12, 2, 2, "123456")
            .print()
    }

    private fun printBarcode() {
        printer.sizeMm(60.0, 30.0)
            .gapMm(0.0, 0.0)
            .cls()
            .barcode(60, 50, TSPLConst.CODE_TYPE_128, 108, "abcdef12345")
            .print()
    }

    private fun printPicCode(b: Bitmap) {
        printer.sizeMm(76.0, 300.0)
            .cls()
            .bitmap(0, 0, TSPLConst.BMP_MODE_OVERWRITE, 600, b, AlgorithmType.Threshold)
            .print(1)
    }

}