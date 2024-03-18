import { Chip } from "@mui/material";
import { useEffect, useState } from "react";

//categories에서 id에 해당하는 categoryName 반환
export default function CategoryNameFromId({ categories, id }) {
  const [name, setName] = useState("");
  useEffect(() => {
    if (categories && id) {
      const category = categories.find((category) => category.categoryId === id);
      setName(category.categoryName);
    }
  }, [categories, id]);

  return <Chip label={name} sx={{ mb: 2 }} />;
}
