//서버에서 받은 LocalDateTime을 필요한 형식으로 변환
export default function FormattedDate({ localDateTime, type }) {
  const originalDate = new Date(localDateTime);
  const year = originalDate.getFullYear();
  const month = ("0" + (originalDate.getMonth() + 1)).slice(-2);
  const date = ("0" + originalDate.getDate()).slice(-2);
  const hours = ("0" + originalDate.getHours()).slice(-2);
  const minutes = ("0" + originalDate.getMinutes()).slice(-2);
  if (type === "time") {
    //2024-01-01 01:01
    return `${year}-${month}-${date} ${hours}:${minutes}`;
  } else if (type === "date") {
    //2024-01-01
    return `${year}-${month}-${date}`;
  } else {
    //2024년 01월 01일 01:01
    return `${year}년 ${month}월 ${date}일 ${hours}:${minutes}`;
  }
}
