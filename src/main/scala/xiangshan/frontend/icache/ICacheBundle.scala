package xiangshan.frontend.icache

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.util._
import freechips.rocketchip.tilelink.ClientMetadata
import xiangshan._
import utils._


class ICacheReadBundle(implicit p: Parameters) extends ICacheBundle
{
  val isDoubleLine  = Bool()
  val vSetIdx       = Vec(2,UInt(log2Ceil(nSets).W))
}

class ICacheMetaRespBundle(implicit p: Parameters) extends ICacheBundle
{
  val metaData   = Vec(2, Vec(nWays, new ICacheMetadata))
  val valid      = Vec(2, Vec(nWays ,Bool()))
  val errors     = Vec(2, Vec(nWays ,Bool()))

  def tags = VecInit(metaData.map(meta_way => VecInit(meta_way.map( meta=> meta.tag ))))
  def cohs = VecInit(metaData.map(meta_way => VecInit(meta_way.map( meta=> meta.coh ))))
}

class ICacheMetaWriteBundle(implicit p: Parameters) extends ICacheBundle
{
  val virIdx  = UInt(idxBits.W)
  val phyTag  = UInt(tagBits.W)
  val coh     = new ClientMetadata
  val waymask = UInt(nWays.W)
  val bankIdx = Bool()

  def generate(tag:UInt, coh: ClientMetadata, idx:UInt, waymask:UInt, bankIdx: Bool){
    this.virIdx  := idx
    this.phyTag  := tag
    this.coh     := coh
    this.waymask := waymask
    this.bankIdx   := bankIdx
  }

}

class ICacheDataWriteBundle(implicit p: Parameters) extends ICacheBundle
{
  val virIdx  = UInt(idxBits.W)
  val data    = UInt(blockBits.W)
  val waymask = UInt(nWays.W)
  val bankIdx = Bool()

  def generate(data:UInt, idx:UInt, waymask:UInt, bankIdx: Bool){
    this.virIdx  := idx
    this.data    := data
    this.waymask := waymask
    this.bankIdx := bankIdx
  }

}

class ICacheDataRespBundle(implicit p: Parameters) extends ICacheBundle
{
  val datas = Vec(2,Vec(nWays,UInt(blockBits.W)))
  val errors = Vec(2, Vec(nWays ,Bool() ))
}

class ICacheMetaReadBundle(implicit p: Parameters) extends ICacheBundle
{
    val req     = Flipped(DecoupledIO(new ICacheReadBundle))
    val resp = Output(new ICacheMetaRespBundle)
}

class ICacheCommonReadBundle(isMeta: Boolean)(implicit p: Parameters) extends ICacheBundle
{
    val req     = Flipped(DecoupledIO(new ICacheReadBundle))
    val resp    = if(isMeta) Output(new ICacheMetaRespBundle) else Output(new ICacheDataRespBundle)
}
