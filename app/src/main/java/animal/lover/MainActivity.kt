package animal.lover

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import animal.lover.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.couchbase.lite.*
import java.io.ByteArrayOutputStream

private const val DOC_ID = "animallover"
private const val TYPE_KEY = "type"
private const val TYPE_VALUE = "animallover"
private const val TEXT_KEY = "text"
private const val IMAGE_KEY = "image"

class MainActivity : AppCompatActivity() {
    private val removeBlob: Boolean = false
    private lateinit var listenerToken: ListenerToken
    private lateinit var binding: ActivityMainBinding
    private lateinit var replicator: Replicator
    private lateinit var database: Database
    private lateinit var liveQuery: Query
    private lateinit var changeListener: QueryChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initCouchbase()
        initListeners()
    }

    private fun initListeners() {
        binding.btnDogLover.setOnClickListener {
            val document: Document? = database.getDocument(DOC_ID)
            val mutableDocument: MutableDocument = document?.toMutable() ?: MutableDocument(DOC_ID)
            if (removeBlob) {
                mutableDocument.remove(IMAGE_KEY)
                database.save(mutableDocument)
            }
            mutableDocument.setString(TYPE_KEY, TYPE_VALUE)
            mutableDocument.setString(TEXT_KEY, "I love dogs!")
            val image = BitmapFactory.decodeResource(
                resources,
                R.drawable.dogs
            )
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            image.recycle()
            mutableDocument.setBlob(
                IMAGE_KEY,
                Blob("image/jpeg", byteArray)
            )
            database.save(mutableDocument)
        }
        binding.btnCatLover.setOnClickListener {
            val document: Document? = database.getDocument(DOC_ID)
            val mutableDocument: MutableDocument = document?.toMutable() ?: MutableDocument(DOC_ID)
            if (removeBlob) {
                mutableDocument.remove(IMAGE_KEY)
                database.save(mutableDocument)
            }
            mutableDocument.setString(TYPE_KEY, TYPE_VALUE)
            mutableDocument.setString(TEXT_KEY, "I love cats!")
            val image = BitmapFactory.decodeResource(
                resources,
                R.drawable.cat
            )
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            image.recycle()
            mutableDocument.setBlob(
                IMAGE_KEY,
                Blob("image/jpeg", byteArray)
            )
            database.save(mutableDocument)
        }
    }

    private fun initCouchbase() {
        CouchbaseLite.init(this)
        val configuration = DatabaseConfiguration()
        database = Database(
            "animal_db",
            configuration
        )
    }

    override fun onResume() {
        super.onResume()
        setupReplicator()
        setupLiveQuery()

    }

    override fun onPause() {
        super.onPause()
        liveQuery.removeChangeListener(listenerToken)
    }

    private fun setupReplicator() {
        try {
            val config = ReplicatorHelper.getReplicatorConfiguration(database)
            replicator = Replicator(config)
            replicator.start(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupLiveQuery() {
        if (!this::liveQuery.isInitialized)
            liveQuery = QueryBuilder.select(SelectResult.property(TEXT_KEY))
                .from(DataSource.database(database))
                .where(
                    Expression.property(TYPE_KEY).equalTo(Expression.string(TYPE_VALUE))
                )
        changeListener = QueryChangeListener { change: QueryChange ->
            change.results?.forEach { result ->
                binding.tv.text = result.getString(TEXT_KEY)
                setImage()
                printDocumentDetails()
            }
        }
        listenerToken = liveQuery.addChangeListener(changeListener)
    }

    private fun printDocumentDetails() {
        val doc = database.getDocument(DOC_ID) ?: return
        val text = """${doc.id}
            |revisionID:
            |${doc.revisionID}
            |sequence:
            |${doc.sequence}
            |doc.text:
            |${doc.getString(TEXT_KEY)}
            |doc.image
            |${doc.getBlob(IMAGE_KEY)}
        """.trimMargin()
        binding.tvDocumentDetails.text = text
    }

    private fun setImage() {
        val doc = database.getDocument(DOC_ID) ?: return
        val blob = doc.getBlob(IMAGE_KEY)
        Glide.with(this).asDrawable().load(
            blob?.content
        )
            .into(object : CustomTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    binding.imageView.setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}