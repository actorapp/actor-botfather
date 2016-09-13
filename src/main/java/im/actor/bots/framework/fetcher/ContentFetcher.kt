package im.actor.bots.framework.fetcher

import akka.actor.UntypedActor
import shardakka.ShardakkaExtension
import shardakka.keyvalue.SimpleKeyValueJava

abstract class ContentFetcher(val storageId: String) : UntypedActor() {

    private val stateKeyValue: SimpleKeyValueJava<String> =
            ShardakkaExtension.get(context().system()).simpleKeyValue("fetcher_$storageId").asJava()

    var isInvalidated = false

    override fun preStart() {
        super.preStart()

        onInvalidate()
    }

    override fun onReceive(p0: Any?) {
        if (p0 is FetcherInvalidate) {

        } else if (p0 is Runnable) {
            p0.run()
        } else {
            // Notify Drop?
        }
    }

    fun onInvalidate() {
        if (!isInvalidated) {
            isInvalidated = true
            doFetch()
        }
    }

    fun onContentAvailable(content: List<Content>) {
        // Called when content available
    }


    //
    // Fetching
    //

    abstract fun doFetch()

    fun onFetched(content: List<Content>) {
        self().tell(Runnable {
            if (content.size > 0) {
                onContentAvailable(content)
            }
            isInvalidated = false
        }, self());
    }
}

abstract class FetcherMessage
class FetcherInvalidate : FetcherMessage()
class FetcherNewContent(val content: List<Content>) : FetcherMessage()

interface OnContentAvailableListener {
    fun onContentAvailable(content: List<Content>)
}

class Content(val id: String, val date: Long, val title: String?, val content: List<ContentPiece>)
abstract class ContentPiece
open class ContentPieceText(val text: String) : ContentPiece()
open class ContentPieceImage(url: String) : ContentPieceFile(url)
open class ContentPieceFile(val url: String) : ContentPiece()