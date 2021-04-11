package net.minecraftforge.minecraftforge.common.model;

import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Optional;

public interface IModelState
{
    Optional<TRSRTransformation> apply(Optional <? extends IModelPart > var1);
}
