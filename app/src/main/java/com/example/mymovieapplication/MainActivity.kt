package com.example.mymovieapplication
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
class MainActivity : AppCompatActivity() {
    private lateinit var posterImage: ImageView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private val images = listOf(
        R.drawable.poster1,
        R.drawable.poster2,
        R.drawable.poster3
    )
    private var currentImageIndex = 0
    private var ticketCount = 1
    private lateinit var ticketCountText: TextView
    private lateinit var btnPlus: TextView
    private lateinit var btnMinus: TextView
    private lateinit var radioGroupAge: RadioGroup
    private lateinit var btnSelectDate: Button
    private lateinit var selectedDateTv: TextView
    private lateinit var theatreSpinner: Spinner
    private lateinit var vipCheckbox: CheckBox
    private lateinit var totalPriceTv: TextView
    private lateinit var btnGetTickets: Button
    private lateinit var btnShowFullSummary: TextView
    private lateinit var summaryShort: TextView
    private var selectedDate: Calendar? = null
    private val basePriceAdult = 12
    private val basePriceChild = 8
    private val vipExtra = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        posterImage = findViewById(R.id.posterImage)
        btnPrev = findViewById(R.id.btnPrevImage)
        btnNext = findViewById(R.id.btnNextImage)
        summaryShort = findViewById(R.id.summaryShort)
        btnShowFullSummary = findViewById(R.id.btnShowFullSummary)
        btnPlus = findViewById<TextView>(R.id.btnPlus)
        btnMinus = findViewById<TextView>(R.id.btnMinus)
        ticketCountText = findViewById(R.id.ticketCount)
        ticketCountText.text = ticketCount.toString()
        radioGroupAge = findViewById(R.id.radioGroupAge)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        selectedDateTv = findViewById(R.id.selectedDate)
        theatreSpinner = findViewById(R.id.theatreSpinner)
        vipCheckbox = findViewById(R.id.checkboxVIP)
        totalPriceTv = findViewById(R.id.totalPrice)
        btnGetTickets = findViewById(R.id.btnGetTickets)

        val theatres = resources.getStringArray(R.array.theatres_list).toList()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, theatres)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        theatreSpinner.adapter = spinnerAdapter
        posterImage.setImageResource(images[currentImageIndex])
        btnPrev.setOnClickListener {
            currentImageIndex = (currentImageIndex - 1 + images.size) % images.size
            animatePosterChange()
            posterImage.setImageResource(images[currentImageIndex])
        }
        btnNext.setOnClickListener {
            currentImageIndex = (currentImageIndex + 1) % images.size
            animatePosterChange()
            posterImage.setImageResource(images[currentImageIndex])
        }
        btnPlus.setOnClickListener {
            if (ticketCount < 10) {
                ticketCount++
                ticketCountText.text = ticketCount.toString()
                updateTotalPrice()
            }
        }
        btnMinus.setOnClickListener {
            if (ticketCount > 1) {
                ticketCount--
                ticketCountText.text = ticketCount.toString()
                updateTotalPrice()
            }
        }
        btnSelectDate.setOnClickListener {
            val now = Calendar.getInstance()
            val dp = DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = Calendar.getInstance()
                    selectedDate!!.set(year, month, day)
                    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    selectedDateTv.text = fmt.format(selectedDate!!.time)
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )

            dp.datePicker.minDate = System.currentTimeMillis()
            dp.show()
        }

        vipCheckbox.setOnCheckedChangeListener { _, _ -> updateTotalPrice() }
        radioGroupAge.setOnCheckedChangeListener { _, _ -> updateTotalPrice() }
        btnGetTickets.setOnClickListener { showOrderSummary() }
        btnShowFullSummary.setOnClickListener {
            showFullSummaryDialog()
        }

        updateTotalPrice()
    }
    private fun updateTotalPrice() {
        val count = ticketCount
        val isAdult = radioGroupAge.checkedRadioButtonId == R.id.radioAdult ||
                radioGroupAge.checkedRadioButtonId == -1
        val base = if (isAdult) basePriceAdult else basePriceChild
        val vip = if (vipCheckbox.isChecked) vipExtra else 0
        val total = (base + vip) * count
        totalPriceTv.text = getString(R.string.total_price_zero).replace("$0", "$$total")
    }
    @SuppressLint("SetTextI18n")
    private fun showOrderSummary() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_order_summary, null)
        val dialogDetails = view.findViewById<TextView>(R.id.dialogDetails)
        val dialogCancel = view.findViewById<Button>(R.id.dialogCancel)
        val dialogConfirm = view.findViewById<Button>(R.id.dialogConfirm)

        val count = ticketCount
        val isAdult = radioGroupAge.checkedRadioButtonId == R.id.radioAdult
        val ageText = if (isAdult) getString(R.string.adult) else getString(R.string.child)

        val theatre = theatreSpinner.selectedItem.toString()
        val dateText = selectedDate?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time)
        } ?: getString(R.string.no_date_selected)

        val vip = if (vipCheckbox.isChecked) getString(R.string.vip_seating) else ""
        val total = totalPriceTv.text.toString()

        dialogDetails.text = """
            ${getString(R.string.movie_title)}
            Tickets: $count
            Type: $ageText
            Date: $dateText
            Theatre: $theatre
            $vip
            $total
        """.trimIndent()

        val alert = AlertDialog.Builder(this).setView(view).create()

        dialogCancel.setOnClickListener { alert.dismiss() }
        dialogConfirm.setOnClickListener {
            alert.dismiss()
            Toast.makeText(this, getString(R.string.order_placed), Toast.LENGTH_LONG).show()
        }

        alert.show()
    }
    private fun showFullSummaryDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_full_summary, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()
        val btnClose = view.findViewById<TextView>(R.id.btnCloseSummary)
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun animatePosterChange() {
        val slide = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        val fade = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        posterImage.startAnimation(slide)
        posterImage.startAnimation(fade)
    }
}
