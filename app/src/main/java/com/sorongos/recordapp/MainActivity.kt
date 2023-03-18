package com.sorongos.recordapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.sorongos.recordapp.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(), OnTimerTickListener {
    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    private enum class State {
        RELEASE, RECORDING, PLAYING
    }

    private lateinit var timer: Timer

    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var fileName: String = ""
    private var state: State = State.RELEASE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //절대경로
        fileName = "${externalCacheDir?.absolutePath}/audioRecordTest.3gp"
        timer = Timer(this)

        binding.recordButton.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    record()
                }
                State.RECORDING -> {
                    onRecord(false)
                }
                State.PLAYING -> {

                }
            }
        }

        binding.playButton.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    onPlay(true)
                }
                else -> {} // do nothing
            }
        }
        binding.stopButton.setOnClickListener {
            when (state) {
                State.PLAYING -> {
                    onPlay(false)
                }
                else -> {} //do nothing
            }
        }
    }

    private fun onPlay(start: Boolean) = if (start) startPlaying() else stopPlaying()

    /**재생중지*/
    private fun startPlaying() {
        state = State.PLAYING

        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
            } catch (e: IOException) {
                Log.e("app", "media player prepare fail $e")
            }
            start()
        }

        binding.waveFormView.clearWave()
        timer.start()

        //녹음 끝
        player?.setOnCompletionListener {
            stopPlaying()
        }

        binding.recordButton.isEnabled = false
        binding.recordButton.alpha = 0.3f
    }

    private fun stopPlaying() {
        state = State.RELEASE

        player?.release()
        player = null

        timer.stop()

        binding.recordButton.isEnabled = true
        binding.recordButton.alpha = 1f
    }

    private fun record() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 실제 녹음 시작
                onRecord(true)
            }
            //교육용 팝업
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                showPermissionInfoDialog()
            }
            //진짜 팝업
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestRecordAudio()
            }
        }
    }

    //start인지 확인
    private fun onRecord(start: Boolean) = if (start) startRecording() else stopRecording()

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        timer.stop()

        state = State.RELEASE

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.baseline_fiber_manual_record_24
            )
        )
        binding.recordButton.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
        binding.playButton.isEnabled = true
        binding.playButton.alpha = 1f
    }


    private fun startRecording() {
        state = State.RECORDING

        recorder = MediaRecorder().apply {
            //마이크 사용하겠다
            setAudioSource(MediaRecorder.AudioSource.MIC)
            //포맷 설정
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            //파일 이름
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("app", "prepare() failed $e")
            }

            start()
        }

        binding.waveFormView.clearData()
        timer.start()

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.baseline_stop_24
            )
        )
        binding.recordButton.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        binding.playButton.isEnabled = false
        binding.playButton.alpha = 0.3f

    }

    private fun requestRecordAudio() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_CODE
        )
    }

    private fun showPermissionInfoDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("녹음을 위해 권한이 필요합니다.")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _, _ ->
                requestRecordAudio()
            }
        }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_CODE //boolean
                && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (audioRecordPermissionGranted) {
            onRecord(true)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                showPermissionInfoDialog()
            } else { // 교육용 팝업을 봤는데도 허용 x
                showPermissionSettingDialog()
            }
        }
    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("녹음 권한을 켜주셔야 앱을 정상적으로 사용할 수 있습니다. 설정에서 권한을 허용해 주세요.")
            setNegativeButton("취소", null)
            setPositiveButton("설정으로 이동") { _, _ ->
                navigate2AppSetting()
            }
        }.show()
    }

    /**intent 함수*/
    private fun navigate2AppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onTick(duration: Long) {
        val millisecond = duration % 1000
        val second = (duration / 1000) % 60
        val minute = (duration / 1000 / 60)

        binding.timerTextView.text =
            String.format("%02d:%02d.%02d", minute, second, millisecond / 10)

        if (state == State.PLAYING) {
            binding.waveFormView.replayAmplitude()
        } else if (state == State.RECORDING) {
            binding.waveFormView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }
}